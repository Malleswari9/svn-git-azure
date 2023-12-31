/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.client.mock;

import org.sigmah.shared.map.AbstractCoordinateEditor;

import java.text.NumberFormat;

/**
 * @author Alex Bertram (akbertram@gmail.com)
 */
public class JreCoordinateEditor extends AbstractCoordinateEditor {

    public JreCoordinateEditor(String negHemiChars, String posHemiChars) {
        this.negHemiChars = negHemiChars;
        this.posHemiChars = posHemiChars;
        this.decimalSeperators = ".,";
        this.invalidMinutesMessage = "invalid minutes";
        this.invalidSecondsMessage = "invalid seconds";
        this.noHemisphereMessage = "no hemisphere specified";
        this.tooManyNumbersErrorMessage = "too many numbers";
        this.noNumberErrorMessage ="no numbers";
    }
   
    @Override
    protected Double parseDouble(String s) {
        return Double.parseDouble(s);
    }

    @Override
    protected String formatDDd(double value) {
        NumberFormat fmt =  NumberFormat.getInstance();
        fmt.setMinimumFractionDigits(6);
        fmt.setMaximumFractionDigits(6);
        fmt.setMinimumIntegerDigits(0);

        if(value > 0) {
            return "+" + fmt.format(value);
        } else {
            return fmt.format(value);
        }
    }

    @Override
    protected String formatShortFrac(double value) {
        NumberFormat fmt =  NumberFormat.getInstance();
        fmt.setMinimumFractionDigits(2);
        fmt.setMaximumFractionDigits(2);
        fmt.setMinimumIntegerDigits(0);

        return fmt.format(value);
    }

    @Override
    protected String formatInt(double value) {
        return NumberFormat.getIntegerInstance().format(value);
    }
}
