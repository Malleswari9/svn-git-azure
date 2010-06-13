/*
 * This file is part of ActivityInfo.
 *
 * ActivityInfo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ActivityInfo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ActivityInfo.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2009 Alex Bertram and contributors.
 */

package org.activityinfo.server.endpoint.gwtrpc.handler;

import com.google.inject.Inject;
import org.activityinfo.server.dao.SchemaDAO;
import org.activityinfo.server.domain.*;
import org.activityinfo.shared.command.GetSchema;
import org.activityinfo.shared.command.result.CommandResult;
import org.activityinfo.shared.dto.*;
import org.activityinfo.shared.exception.CommandException;
import org.dozer.Mapper;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Alex Bertram
 * @see org.activityinfo.shared.command.GetSchema
 */
public class GetSchemaHandler implements CommandHandler<GetSchema> {

    private SchemaDAO schemaDAO;
    private Mapper mapper;

    @Inject
    public GetSchemaHandler(SchemaDAO schemaDAO, Mapper mapper) {
        this.schemaDAO = schemaDAO;
        this.mapper = mapper;
    }

    @Override
    public CommandResult execute(GetSchema cmd, User user) throws CommandException {

        SchemaDTO schema = new SchemaDTO();
        Date lastUpdate = new Date(0);

        List<UserDatabase> databases = schemaDAO.getDatabases(user);

        Map<Integer, CountryDTO> countries = new HashMap<Integer, CountryDTO>();

        for (UserDatabase database : databases) {

            if (database.getLastSchemaUpdate().after(lastUpdate))
                lastUpdate = database.getLastSchemaUpdate();

            UserDatabaseDTO databaseDTO = new UserDatabaseDTO();

            databaseDTO.setId(database.getId());
            databaseDTO.setName(database.getName());
            databaseDTO.setFullName(database.getFullName());
            databaseDTO.setOwnerName(database.getOwner().getName());
            databaseDTO.setOwnerEmail(database.getOwner().getEmail());

            CountryDTO country = countries.get(database.getCountry().getId());
            if (country == null) {
                country = mapper.map(database.getCountry(), CountryDTO.class);
                countries.put(country.getId(), country);

                schema.getCountries().add(country);
            }
            databaseDTO.setCountry(country);
            databaseDTO.setAmOwner(database.getOwner().getId() == user.getId());

            UserPermission permission = null;
            if (!databaseDTO.getAmOwner()) {
                databaseDTO.setMyPartnerId(
                        database.getPermissionByUser(user).getPartner().getId());

                permission = database.getPermissionByUser(user);

                if (permission.getLastSchemaUpdate().after(lastUpdate))
                    lastUpdate = permission.getLastSchemaUpdate();
            }

            databaseDTO.setViewAllAllowed(databaseDTO.getAmOwner() || permission.isAllowViewAll());
            databaseDTO.setEditAllowed(databaseDTO.getAmOwner() || permission.isAllowEdit());
            databaseDTO.setEditAllAllowed(databaseDTO.getAmOwner() || permission.isAllowEditAll());
            databaseDTO.setDesignAllowed(databaseDTO.getAmOwner() || permission.isAllowDesign());
            databaseDTO.setManageUsersAllowed(databaseDTO.getAmOwner() || permission.isAllowManageUsers());
            databaseDTO.setManageAllUsersAllowed(databaseDTO.getAmOwner() || permission.isAllowManageAllUsers());

            for (Partner partner : database.getPartners()) {

                databaseDTO.getPartners().add(mapper.map(partner, PartnerDTO.class));
            }

            for (Activity activity : database.getActivities()) {

                ActivityDTO activityDTO = mapper.map(activity, ActivityDTO.class);
                databaseDTO.getActivities().add(activityDTO);
                activityDTO.setDatabase(databaseDTO);

            }

            schema.getDatabases().add(databaseDTO);

        }

        schema.setVersion(lastUpdate.getTime());

        return schema;


    }
}
