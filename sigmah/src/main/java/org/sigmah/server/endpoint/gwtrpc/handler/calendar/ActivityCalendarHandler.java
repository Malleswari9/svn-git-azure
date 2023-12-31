/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */
package org.sigmah.server.endpoint.gwtrpc.handler.calendar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sigmah.shared.domain.calendar.ActivityCalendarIdentifier;
import org.sigmah.shared.domain.calendar.Calendar;
import org.sigmah.shared.domain.calendar.Event;
import org.sigmah.shared.domain.logframe.ExpectedResult;
import org.sigmah.shared.domain.logframe.LogFrame;
import org.sigmah.shared.domain.logframe.LogFrameActivity;
import org.sigmah.shared.domain.logframe.SpecificObjective;

import com.google.inject.Inject;

/**
 * Convert activities as calendar events.
 * @author Raphaël Calabro (rcalabro@ideia.fr)
 */
public class ActivityCalendarHandler implements CalendarHandler {

    private final static Log log = LogFactory.getLog(ActivityCalendarHandler.class);
    private EntityManager em;

    @Inject
    public ActivityCalendarHandler(EntityManager em) {
        this.em = em;
    }

    @Override
    public Calendar getCalendar(Serializable identifier) {
        if (!(identifier instanceof ActivityCalendarIdentifier)) {
            throw new IllegalArgumentException("Identifier must be an instance of ActivityCalendarIdentifier, received an instance of " + identifier.getClass().getSimpleName());
        }

        final ActivityCalendarIdentifier activityCalendarIdentifier = (ActivityCalendarIdentifier) identifier;

        em.clear();
        final Query query = em.createQuery("SELECT l FROM LogFrame l WHERE l.parentProject.id = :projectId");
        query.setParameter("projectId", activityCalendarIdentifier.getProjectId());

        // Configuring the calendar
        final Calendar calendar = new Calendar();
        calendar.setIdentifier(identifier);
        calendar.setName(activityCalendarIdentifier.getCalendarName());
        calendar.setEditable(false);

        final HashMap<Date, List<Event>> eventMap = new HashMap<Date, List<Event>>();
        calendar.setEvents(eventMap);

        try {
            final LogFrame logFrame = (LogFrame) query.getSingleResult();

            // Preparing the activity code
            final StringBuilder codeBuilder = new StringBuilder(activityCalendarIdentifier.getActivityPrefix());
            codeBuilder.append(' ');

            // Looping on the logical framework objects
            final List<SpecificObjective> specificObjectives = logFrame.getSpecificObjectives();
            for (final SpecificObjective specificObjective : specificObjectives) {
                int baseSize = codeBuilder.length();
                codeBuilder.append((char) ('A' + specificObjective.getCode() - 1));
                codeBuilder.append('.');

                final List<ExpectedResult> expectedResults = specificObjective.getExpectedResults();
                for (final ExpectedResult expectedResult : expectedResults) {
                    int specificObjectiveSize = codeBuilder.length();
                    codeBuilder.append(expectedResult.getCode());
                    codeBuilder.append('.');

                    // For each activity
                    final List<LogFrameActivity> activities = expectedResult.getActivities();
                    for (final LogFrameActivity activity : activities) {
                        int expectedResultSize = codeBuilder.length();
                        codeBuilder.append(activity.getCode());
                        codeBuilder.append('.');

						final Date startDate = activity.getStartDate();
						
                        if(activity.getTitle() != null) {
                            codeBuilder.append(' ');
                            codeBuilder.append(activity.getTitle());
                        }
                    
                        // For each day
                        if (startDate != null) {
                        	//if activity end date is not spécified set its value to start date
							if (activity.getEndDate() == null) {
								activity.setEndDate(startDate);
							}
                        	
                            for (Date date = new Date(startDate.getYear(), startDate.getMonth(), startDate.getDate());
                                    date.compareTo(activity.getEndDate()) < 1; date.setDate(date.getDate() + 1)) {
                                final Date key = new Date(date.getTime());

                                final Event event = new Event();
                                event.setSummary(codeBuilder.toString());
                                event.setDtstart(new Date(startDate.getTime()));

                                if(startDate.equals(activity.getEndDate())) {
                                    event.setDtend(new Date(startDate.getYear(), startDate.getMonth(), startDate.getDate()+1));
                                } else {
                                    event.setDtend(new Date(activity.getEndDate().getTime()));
                                }
                                
                                event.setParent(calendar);
                                event.setIdentifier(activity.getId());

                                // Adding the event to the event map
                                List<Event> list = eventMap.get(key);
                                if (list == null) {
                                    list = new ArrayList<Event>();
                                    eventMap.put(key, list);
                                }
                                list.add(event);
                            }
                        }

                        codeBuilder.setLength(expectedResultSize);
                    }

                    codeBuilder.setLength(specificObjectiveSize);
                }

                codeBuilder.setLength(baseSize);
            }
        } catch (NoResultException e) {
            // No activities in the current project
        }


        return calendar;
    }
}
