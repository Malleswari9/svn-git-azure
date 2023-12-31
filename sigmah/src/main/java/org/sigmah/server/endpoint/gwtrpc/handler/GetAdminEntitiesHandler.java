/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.server.endpoint.gwtrpc.handler;

import com.google.inject.Inject;
import org.dozer.Mapper;
import org.sigmah.server.dao.hibernate.AdminHibernateDAO;
import org.sigmah.shared.command.GetAdminEntities;
import org.sigmah.shared.command.handler.CommandHandler;
import org.sigmah.shared.command.result.AdminEntityResult;
import org.sigmah.shared.command.result.CommandResult;
import org.sigmah.shared.dao.AdminDAO;
import org.sigmah.shared.domain.AdminEntity;
import org.sigmah.shared.domain.User;
import org.sigmah.shared.dto.AdminEntityDTO;
import org.sigmah.shared.exception.CommandException;

import java.util.ArrayList;
import java.util.List;

public class GetAdminEntitiesHandler implements CommandHandler<GetAdminEntities> {

    protected AdminDAO adminDAO;
    protected Mapper mapper;

    @Inject
    public GetAdminEntitiesHandler(AdminHibernateDAO adminDAO, Mapper mapper) {
        this.adminDAO = adminDAO;
        this.mapper = mapper;
    }
  
    public GetAdminEntitiesHandler(AdminDAO adminDAO, Mapper mapper) {
        this.adminDAO = adminDAO;
        this.mapper = mapper;
    }
    
    @Override
    public CommandResult execute(GetAdminEntities cmd, User user) throws CommandException {

   //     List<AdminEntity> entities = adminDAO.find(cmd.getLevelId(), cmd.getParentId(), cmd.getActivityId());

        AdminDAO.Query query = adminDAO.query().level(cmd.getLevelId());
     
    
        if (cmd.getParentId() != null) {
        	query.withParentEntityId(cmd.getParentId());
        }
        if (cmd.getActivityId() != null) {
        	query.withSitesOfActivityId(cmd.getActivityId());
        }
        
        List<AdminEntity> entities = query.execute();
      
              
        List<AdminEntityDTO> models = new ArrayList<AdminEntityDTO>();

        for (AdminEntity entity : entities) {
            models.add(mapper.map(entity, AdminEntityDTO.class));
        }

        return new AdminEntityResult(models);
    }
}
