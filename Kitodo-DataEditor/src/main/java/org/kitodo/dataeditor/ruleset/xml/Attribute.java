/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.dataeditor.ruleset.xml;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * A possible option as a member of an enumeration type.
 */
public class Attribute {

    /**
     * The string value.
     */
    @XmlAttribute(required = true)
    private String name;

    /**
     * The string value.
     */
    @XmlAttribute(required = true)
    private String value;


    /**
     * Return function for the value.
     *
     * @return the value
     */
    public String getName() {
        return name;
    }


    /**
     * Return function for the value.
     * 
     * @return the value
     */
    public String getValue() {
        return value;
    }

}
