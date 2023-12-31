/*
 * All Sigmah code is released under the GNU General Public License v3 See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.server.endpoint.gwtrpc.handler;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import org.sigmah.server.util.ReportingPeriodValidation;
import org.sigmah.shared.command.Month;
import org.sigmah.shared.command.UpdateMonthlyReports;
import org.sigmah.shared.command.handler.CommandHandler;
import org.sigmah.shared.command.result.CommandResult;
import org.sigmah.shared.command.result.VoidResult;
import org.sigmah.shared.domain.Indicator;
import org.sigmah.shared.domain.IndicatorValue;
import org.sigmah.shared.domain.ReportingPeriod;
import org.sigmah.shared.domain.Site;
import org.sigmah.shared.domain.User;
import org.sigmah.shared.exception.CommandException;

import com.google.inject.Inject;

/**
 * @author Alex Bertram
 * @see org.sigmah.shared.command.UpdateMonthlyReports
 */
public class UpdateMonthlyReportsHandler implements CommandHandler<UpdateMonthlyReports> {

    private final EntityManager em;

    @Inject
    public UpdateMonthlyReportsHandler(EntityManager em) {
        this.em = em;
    }

    public CommandResult execute(UpdateMonthlyReports cmd, User user) throws CommandException {

        Site site = em.find(Site.class, cmd.getSiteId());

        Map<Month, ReportingPeriod> periods = new HashMap<Month, ReportingPeriod>();

        for (ReportingPeriod period : site.getReportingPeriods()) {
            periods.put(HandlerUtil.monthFromRange(period.getDate1(), period.getDate2()), period);
        }

        for (UpdateMonthlyReports.Change change : cmd.getChanges()) {

            ReportingPeriod period = periods.get(change.month);
            if (period == null) {
                period = new ReportingPeriod(site);

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, change.month.getYear());
                calendar.set(Calendar.MONTH, change.month.getMonth() - 1);
                calendar.set(Calendar.DATE, 5);
                period.setDate1(calendar.getTime());

                calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE) - 5);
                period.setDate2(calendar.getTime());

                em.persist(period);

                periods.put(change.month, period);
            } else {
                boolean wasValid = ReportingPeriodValidation.validate(period);
                if (!wasValid) {
                    em.merge(period);
                }
            }

            updateIndicatorValue(em, period, change.indicatorId, change.value, false);
        }

        return new VoidResult();

    }

    public void updateIndicatorValue(EntityManager em, ReportingPeriod period, int indicatorId, Double value,
            boolean creating) {

        if (value == null && !creating) {
            int rowsAffected =
                    em.createQuery("delete IndicatorValue v where v.indicator.id = ?1 and v.reportingPeriod.id = ?2")
                        .setParameter(1, indicatorId).setParameter(2, period.getId()).executeUpdate();

            assert rowsAffected <= 1 : "whoops, deleted too many";

        } else if (value != null) {

            int rowsAffected = 0;

            if (!creating) {
                rowsAffected =
                        em.createQuery(
                            "update IndicatorValue v set v.value = ?1 where "
                                + "v.indicator.id = ?2 and "
                                + "v.reportingPeriod.id = ?3").setParameter(1, (Double) value)
                            .setParameter(2, indicatorId).setParameter(3, period.getId()).executeUpdate();
            }

            if (rowsAffected == 0) {

                IndicatorValue iValue =
                        new IndicatorValue(period, em.getReference(Indicator.class, indicatorId), (Double) value);

                em.persist(iValue);

            }
        }

    }

}
