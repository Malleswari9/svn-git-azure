package org.sigmah.server.endpoint.file;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sigmah.server.dao.Transactional;
import org.sigmah.server.endpoint.gwtrpc.handler.GetOrgUnitHandler;
import org.sigmah.server.endpoint.gwtrpc.handler.GetProjectHandler;
import org.sigmah.shared.command.result.ValueResultUtils;
import org.sigmah.shared.domain.OrgUnit;
import org.sigmah.shared.domain.Project;
import org.sigmah.shared.domain.User;
import org.sigmah.shared.domain.element.FilesListElement;
import org.sigmah.shared.domain.element.FlexibleElement;
import org.sigmah.shared.domain.reminder.MonitoredPoint;
import org.sigmah.shared.domain.reminder.MonitoredPointList;
import org.sigmah.shared.domain.value.File;
import org.sigmah.shared.domain.value.FileVersion;
import org.sigmah.shared.domain.value.Value;
import org.sigmah.shared.dto.value.FileUploadUtils;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Manages files (upload and download).
 * 
 * @author tmi
 * @author Aurélien Ponçon
 */
public class FileManagerImpl implements FileManager {

	/**
	 * Logger.
	 */
	private static final Log log = LogFactory.getLog(FileManagerImpl.class);

	private final Provider<EntityManager> entityManager;

	private final FileStorageProvider fileStorageProvider;

	@Inject
	public FileManagerImpl(Provider<EntityManager> entityManager, FileStorageProvider storageProvider) {
		this.entityManager = entityManager;
		this.fileStorageProvider = storageProvider;
	}

	@Override
	public String save(Map<String, String> properties, byte[] content) throws IOException {

		// Uploaded file's id.
		String id = properties.get(FileUploadUtils.DOCUMENT_ID);

		// Author id.
		String authorIdProp = properties.get(FileUploadUtils.DOCUMENT_AUTHOR);
		int authorId;

		try {
			if (authorIdProp == null) {
				authorId = 0;
			} else {
				authorId = Integer.valueOf(authorIdProp);
			}
		} catch (NumberFormatException e) {
			authorId = 0;
		}

		// New file (first version).
		if (id == null) {
			id = saveNewFile(properties, content, authorId);
		}
		// New version.
		else {
			id = saveNewVersion(properties, content, id, authorId);
		}

		return id;
	}

	/**
	 * Saves a new file.
	 * 
	 * @param properties
	 *            The properties map of the uploaded file (see
	 *            {@link FileUploadUtils}).
	 * @param content
	 *            The uploaded file content.
	 * @param authorId
	 *            The author id.
	 * @return The id of the just saved file.
	 * @throws IOException
	 */
	@Transactional
	protected String saveNewFile(Map<String, String> properties, byte[] content, int authorId) throws IOException {

		final EntityManager em = entityManager.get();

		if (log.isDebugEnabled()) {
			log.debug("[saveNewFile] New file.");
		}

		// --------------------------------------------------------------------
		// STEP 1 : saves the file.
		// --------------------------------------------------------------------

		if (log.isDebugEnabled()) {
			log.debug("[saveNewFile] Saves the new file.");
		}

		final File file = new File();

		// Gets the details of the name of the file.
		final String fullName = validateFileName(properties.get(FileUploadUtils.DOCUMENT_NAME));
		final String name = getFileCanonicalName(fullName);
		final String extension = getFileExtension(fullName);

		file.setName(name);

		// Creates and adds the new version.
		file.addVersion(createVersion(1, name, extension, authorId, content));

		em.persist(file);

		// --------------------------------------------------------------------
		// STEP 2 : gets the current value for this list of files.
		// --------------------------------------------------------------------

		// Element id.
		String elementIdProp = properties.get(FileUploadUtils.DOCUMENT_FLEXIBLE_ELEMENT);
		long elementId;

		try {
			if (elementIdProp == null) {
				elementId = 0;
			} else {
				elementId = Integer.valueOf(elementIdProp);
			}
		} catch (NumberFormatException e) {
			elementId = 0;
		}

		// Project id.
		String projectIdProp = properties.get(FileUploadUtils.DOCUMENT_PROJECT);
		int projectId;

		try {
			if (projectIdProp == null) {
				projectId = 0;
			} else {
				projectId = Integer.valueOf(projectIdProp);
			}
		} catch (NumberFormatException e) {
			projectId = 0;
		}

		// Retrieving the current value
		final Query query = em
		                .createQuery("SELECT v FROM Value v WHERE v.containerId = :projectId and v.element.id = :elementId");
		query.setParameter("projectId", projectId);
		query.setParameter("elementId", elementId);

		final Value currentValue;
		Object object = null;

		try {
			object = query.getSingleResult();
		} catch (NoResultException nre) {
			// No current value
		}

		// --------------------------------------------------------------------
		// STEP 3 : creates or updates the value with the new file id.
		// --------------------------------------------------------------------

		// The value already exists, must update it.
		if (object != null) {
			currentValue = (Value) object;
			currentValue.setLastModificationAction('U');

			// Sets the value (adds a new file id).
			currentValue.setValue(currentValue.getValue() + ValueResultUtils.DEFAULT_VALUE_SEPARATOR
			                + String.valueOf(file.getId()));
		}
		// The value for this list of files doesn't exist already, must
		// create it.
		else {
			currentValue = new Value();

			// Creation of the value
			currentValue.setLastModificationAction('C');

			// Parent element
			final FlexibleElement element = em.find(FlexibleElement.class, elementId);
			currentValue.setElement(element);

			// Container
			currentValue.setContainerId(projectId);

			// Sets the value (one file id).
			currentValue.setValue(String.valueOf(file.getId()));
		}

		// Modifier
		final User user = em.find(User.class, authorId);
		currentValue.setLastModificationUser(user);

		// Last update date
		currentValue.setLastModificationDate(new Date());

		// Saves or updates the new value.
		em.merge(currentValue);

		return String.valueOf(file.getId());
	}

	/**
	 * Saves a new file.
	 * 
	 * @param properties
	 *            The properties map of the uploaded file (see
	 *            {@link FileUploadUtils}).
	 * @param content
	 *            The uploaded file content.
	 * @param id
	 *            The file which gets a new version.
	 * @param authorId
	 *            The author id.
	 * @return The file id (must be the same as the parameter).
	 * @throws IOException
	 */
	@Transactional
	protected String saveNewVersion(Map<String, String> properties, byte[] content, String id, int authorId)
	                throws IOException {

		final EntityManager em = entityManager.get();

		if (log.isDebugEnabled()) {
			log.debug("[save] New file version.");
		}

		// Gets the details of the name of the file.
		final String fullName = validateFileName(properties.get(FileUploadUtils.DOCUMENT_NAME));
		final String name = getFileCanonicalName(fullName);
		final String extension = getFileExtension(fullName);

		// Creates and adds the new version.
		final File file = em.find(File.class, Integer.valueOf(id));

		if (log.isDebugEnabled()) {
			log.debug("[save] Found file: " + file.getName() + ".");
		}

		Integer versionNumber;

		Query query = em.createQuery("SELECT max(fv.versionNumber)+1 AS newVersionNumber FROM FileVersion AS fv WHERE parentFile=:parentFile");
		query.setParameter("parentFile", file);
		versionNumber = (Integer) query.getSingleResult();
		if (versionNumber == null) {
			versionNumber = 0;
		}

		final FileVersion version = createVersion(versionNumber, name, extension, authorId, content);
		version.setComments(properties.get(FileUploadUtils.DOCUMENT_COMMENTS));
		file.addVersion(version);

		em.persist(file);

		return String.valueOf(file.getId());
	}

	/**
	 * Creates a file version with the given number and author.
	 * 
	 * @param versionNumber
	 *            The version number.
	 * @param name
	 *            The version name.
	 * @param extension
	 *            The version extension.
	 * @param authorId
	 *            The author id.
	 * @param content
	 *            The version content.
	 * @return The version just created.
	 * @throws IOException
	 */
	private FileVersion createVersion(int versionNumber, String name, String extension, int authorId, byte[] content)
	                throws IOException {

		if (log.isDebugEnabled()) {
			log.debug("[createVersion] Creates a new file version # + " + versionNumber + ".");
		}

		final FileVersion version = new FileVersion();

		// Sets attributes.
		version.setVersionNumber(versionNumber);
		version.setName(name);
		version.setExtension(extension);
		version.setAddedDate(new Date());
		version.setSize(Long.valueOf(content.length));
		final User user = new User();
		user.setId(authorId);
		version.setAuthor(user);

		// Saves content.
		version.setPath(writeContent(content));

		return version;
	}

	/**
	 * Writes the file content.
	 * 
	 * @param content
	 *            The content as bytes array.
	 * @return The unique string identifier to identify the just saved file.
	 * @throws IOException
	 */
	public String writeContent(byte[] content) throws IOException {

		OutputStream output = null;

		try {

			// Generates the content file name
			final String uniqueName = generateUniqueName();

			// Streams.
			output = fileStorageProvider.create(uniqueName);

			// Writes content as bytes.
			output.write(content);

			return uniqueName;
		} finally {
			if (output != null) {
				output.close();
			}
		}
	}

	/**
	 * Computes and returns a unique string identifier to name files.
	 * 
	 * @return A unique string identifier.
	 */
	private static String generateUniqueName() {
		// Adds the timestamp to ensure the id uniqueness.
		return UUID.randomUUID().toString() + new Date().getTime();
	}

	@Override
	@SuppressWarnings("unchecked")
	public RepositoryElement getRepository(OrgUnit orgunit, User user, boolean allVersions) {
		final EntityManager em = entityManager.get();

		if (GetOrgUnitHandler.isOrgUnitVisible(orgunit, user)) {

			FolderElement root = new FolderElement("root", "root");

			Set<OrgUnit> orgUnitTree = new HashSet<OrgUnit>();
			GetProjectHandler.crawlUnits(orgunit, orgUnitTree, true);

			Query filesQuery;

			final Query valuesQuery = em.createQuery("SELECT v " + "FROM Value v " + "WHERE v.containerId IN ("
			                + "SELECT ud.id " + "FROM OrgUnit o " + "INNER JOIN o.databases ud "
			                + "WHERE o IN (:units)" + ") OR v.containerId IN (:units)");

			if (allVersions) {
				// All versions of each file
				filesQuery = em.createQuery("SELECT fv " + "FROM File f " + "INNER JOIN f.versions fv "
				                + "WHERE f.id IN (:idsList)");
			} else {
				// Just the last version of each file
				filesQuery = em.createQuery("SELECT fv " + "FROM File f " + "INNER JOIN f.versions fv "
				                + "WHERE f.id IN (:idsList) " + "AND fv.versionNumber IN "
				                + "(SELECT max(fv2.versionNumber) " + "FROM FileVersion fv2 "
				                + "WHERE fv2.parentFile = f)");
			}

			valuesQuery.setParameter("units", orgUnitTree);
			List<Value> values = valuesQuery.getResultList();

			for (Value v : values) {
				if (v.getElement() instanceof FilesListElement) {
					Project ud = em.find(Project.class, v.getContainerId());
					OrgUnit o = null;

					if (ud != null) {
						Collection<OrgUnit> orgUnitSet = ud.getPartners();
						o = (OrgUnit) orgUnitSet.toArray()[0];
					} else {
						o = em.find(OrgUnit.class, v.getContainerId());
					}

					filesQuery.setParameter("idsList", ValueResultUtils.splitValuesAsInteger(v.getValue()));

					List<FileVersion> versions = filesQuery.getResultList();

					FolderElement orgUnitRepository = (FolderElement) root.getById("o" + o.getId());
					if (orgUnitRepository == null) {
						orgUnitRepository = new FolderElement("o" + o.getId(), validateFileName(o.getFullName()));
						root.appendChild(orgUnitRepository);
					}

					FolderElement fileFolderElementParent = null;

					if (ud != null) {
						fileFolderElementParent = (FolderElement) orgUnitRepository.getById("p" + ud.getId());
						if (fileFolderElementParent == null) {
							fileFolderElementParent = new FolderElement("p" + ud.getId(),
							                validateFileName(ud.getFullName()));
							orgUnitRepository.appendChild(fileFolderElementParent);
						}
					} else {
						fileFolderElementParent = orgUnitRepository;
					}

					for (FileVersion version : versions) {
						if (allVersions) {
							FolderElement fileRepository = (FolderElement) fileFolderElementParent.getById("f"
							                + version.getParentFile().getId());
							if (fileRepository == null) {
								fileRepository = new FolderElement("f" + version.getParentFile().getId(),
								                validateFileName(version.getParentFile().getName()) + "_f"
								                                + version.getParentFile().getId());
								fileFolderElementParent.appendChild(fileRepository);
							}

							FileElement file = new FileElement("fv" + version.getId(),
							                validateFileName(version.getName()) + "_v" + version.getId() + "."
							                                + version.getExtension(), version.getPath());
							fileRepository.appendChild(file);
						} else {
							FileElement file = new FileElement("f" + version.getId(),
							                validateFileName(version.getName()) + "_f" + version.getId() + "."
							                                + version.getExtension(), version.getPath());
							fileFolderElementParent.appendChild(file);
						}
					}
				}
			}
			return root;
		} else {
			return null;
		}
	}

	/**
	 * It deletes the string "C:\fakepath\" which come from an issue in Google
	 * Chrome It replaces also all wrong characters that can't be displayed in a
	 * file name or a directory name by "_"
	 * 
	 * @param fileName
	 *            name to validate
	 * @return string the name validated
	 */
	private String validateFileName(String fileName) {
		return fileName.replaceFirst("[cC]:\\\\fakepath\\\\", "").replaceAll("[\\/:*?\"<>|]", "_");
	}

	@Override
	public DownloadableFile getFile(String idString, String versionString) {

		final EntityManager em = entityManager.get();

		// Gets file id.
		int id;

		try {
			if (idString == null) {
				id = 0;
			} else {
				id = Integer.valueOf(idString);
			}
		} catch (NumberFormatException e) {
			id = 0;
		}

		// Gets the file entity.
		final File file = em.find(File.class, id);

		// Unable to find the file entity -> download error.
		if (file == null) {
			return null;
		}

		if (log.isDebugEnabled()) {
			log.debug("[getFile] Found file with id=" + file.getId());
		}

		FileVersion lastVersion = null;

		// Downloads the last version.
		if (versionString == null || "".equals(versionString)) {
			if (log.isDebugEnabled()) {
				log.debug("[getFile] Searches last version.");
			}

			final List<FileVersion> versions = file.getVersions();

			if (versions == null || versions.isEmpty()) {
				lastVersion = null;
			} else {

				// Searches the max version number which identifies the last
				// version.
				int index = 0;
				int maxVersionNumber = versions.get(index).getVersionNumber();
				for (int i = 1; i < versions.size(); i++) {
					if (versions.get(i).getVersionNumber() > maxVersionNumber) {
						index = i;
					}
				}

				lastVersion = versions.get(index);
			}
		} else {

			// Get desired file version.
			int version;

			try {
				version = Integer.valueOf(versionString);
			} catch (NumberFormatException e) {
				version = 0;
			}

			if (log.isDebugEnabled()) {
				log.debug("[getFile] Searches specific version with number=" + version + ".");
			}

			// Searches this specific version.
			final List<FileVersion> versions = file.getVersions();

			if (versions == null || versions.isEmpty()) {
				lastVersion = null;
			}

			for (final FileVersion v : versions) {
				if (v.getVersionNumber() == version) {
					lastVersion = v;
					break;
				}
			}
		}

		// Unable to find the desired version -> download error.
		if (lastVersion == null) {
			return null;
		}

		if (log.isDebugEnabled()) {
			log.debug("[getFile] Found version with number=" + lastVersion.getVersionNumber() + ".");
		}

		return new DownloadableFile(lastVersion.getName() + '.' + lastVersion.getExtension(), lastVersion.getPath());
	}

	@Transactional
	@Override
	public MonitoredPoint createMonitoredPoint(Integer projetId, String label, Date expectedDate, Integer fileId) {

		final EntityManager em = entityManager.get();

		// Retrieves parent project and points list.
		final Project p = em.find(Project.class, projetId);
		MonitoredPointList list = p.getPointsList();

		if (list == null) {
			list = new MonitoredPointList();
			p.setPointsList(list);
		}

		if (list.getPoints() == null) {
			list.setPoints(new ArrayList<MonitoredPoint>());
		}

		// Creates point.
		final MonitoredPoint point = new MonitoredPoint();
		point.setLabel(label);
		point.setExpectedDate(expectedDate);
		point.setFile(em.find(File.class, fileId));
		point.setCompletionDate(null);
		point.setDeleted(false);

		// Adds the point to the list.
		list.addMonitoredPoint(point);

		// Saves.
		em.persist(p);

		return point;
	}

	/**
	 * Returns the canonical name of a file. <br/>
	 * For example:
	 * <ul>
	 * <li>myfile.txt -> myfile</li>
	 * <li>myfile.txt.other -> myfile.txt</li>
	 * <li>myfile. -> myfile</li>
	 * <li>myfile -> myfile</li>
	 * </ul>
	 * 
	 * @param fullName
	 *            The file name.
	 * @return The file canonical name.
	 */
	private static String getFileCanonicalName(String fullName) {

		// Invalid name.
		if (fullName == null) {
			return null;
		}

		// Searches for the last '.' (before the extension).
		final int index = fullName.lastIndexOf('.');

		// If the file doesn't have an extension, the name is the entire string.
		if (index == -1) {
			return fullName;
		}

		return fullName.substring(0, index);
	}

	/**
	 * Returns the extension of a file. <br/>
	 * For example:
	 * <ul>
	 * <li>myfile.txt -> txt</li>
	 * <li>myfile.txt.other -> other</li>
	 * <li>myfile. -> <code>null</code></li>
	 * <li>myfile -> <code>null</code></li>
	 * </ul>
	 * 
	 * @param fullName
	 *            The file name.
	 * @return The file extension.
	 */
	private static String getFileExtension(String fullName) {

		// Invalid name.
		if (fullName == null) {
			return null;
		}

		// Searches for the last '.' (before the extension).
		final int index = fullName.lastIndexOf('.');

		// The file doesn't have an extension.
		if (index == -1 || index + 1 == fullName.length()) {
			return null;
		}

		return fullName.substring(index + 1);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean isGrantedToDownload(User user, File file) {
		EntityManager em = entityManager.get();
		Query query = em.createQuery("SELECT o FROM OrgUnit o"
		                + "WHERE o.databases.pointsList.points.file = :file AND o=:user.orgUnitWithProfiles.orgUnit");
		query.setParameter("file", file);
		query.setParameter("user", user);
		List<OrgUnit> units = query.getResultList();
		if (units.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}
}
