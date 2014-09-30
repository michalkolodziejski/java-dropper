package org.mkdev.ut.dropper;

/**
 * @author Michał Kołodziejski &lt;<I><A href="mailto:michal.kolodziejski@gmail.com">michal.kolodziejski@gmail.com</A></I>&gt;
 * @version 1.0
 * @license: GPLv3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 * @since: 2014-09-29
 */
public class DropCountExceededException extends Exception {

    private static final long serialVersionUID = 2253293215063842044L;

    public DropCountExceededException(ClassNotFoundException e) {
        super(e);
    }

    public DropCountExceededException() {
        super();
    }

    public DropCountExceededException(String s) {
        super(s);
    }

    public DropCountExceededException(Exception e) {
        super(e);
    }
}
