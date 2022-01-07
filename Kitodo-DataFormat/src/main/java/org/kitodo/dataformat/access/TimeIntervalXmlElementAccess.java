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

package org.kitodo.dataformat.access;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.MdSec;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.MetadataGroup;
import org.kitodo.api.dataeditor.rulesetmanagement.FunctionalMetadata;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.api.dataformat.mets.KitodoUUID;
import org.kitodo.dataformat.metskitodo.AreaType;
import org.kitodo.dataformat.metskitodo.DivType;
import org.kitodo.dataformat.metskitodo.DivType.Fptr;

public class TimeIntervalXmlElementAccess {

    public static final String TIME_INTERVAL_POSITION = "timeIntervalPosition";
    public static final String TIME_INTERVAL_POSITION_END = "end";

    static MetadataEntry readMetadata(DivType div, String potentialLogicalDivisionID) {
       for (DivType childDiv : div.getDiv()) {
            if (PhysicalDivision.TYPE_TRACK.equals(childDiv.getTYPE())
                    && childDiv.getID().equals(potentialLogicalDivisionID)) {
                List<Fptr> fptrs = childDiv.getFptr();
                if(fptrs.size() == 1) {
                    AreaType areaType = fptrs.get(0).getArea();
/*

                    MetadataEntry metadataEntry = new MetadataEntry();
                    metadataEntry.setDomain(MdSec.TIMESTAMP);
                    metadataEntry.setKey(metadataType.getName());
                    metadataEntry.setValue(metadataType.getValue());
                    metadataEntry.setAttributes(TIME_INTERVAL_POSITION);
*/

                }
            }
        }
        return null;
    }

    static DivType toDiv(LogicalDivision logicalDivision, Map<LogicalDivision, String> logicalDivisionIDs,
            LinkedList<Pair<String, String>> smLinkData) {
        DivType parentDiv = new DivType();
        parentDiv.setID(KitodoUUID.randomUUID());

        String logicalDivisionUUID = logicalDivisionIDs.get(logicalDivision);

        for (LogicalDivision logicalDivisionChild : logicalDivision.getChildren()) {
            for (Metadata potentialMetadataGroup : logicalDivisionChild.getMetadata()) {
                if (potentialMetadataGroup instanceof MetadataGroup
                        && MdSec.TIMEINTERVAL.equals(potentialMetadataGroup.getDomain())) {
                    DivType div = new DivType();
                    String timeIntervalUUID = KitodoUUID.randomUUID();
                    div.setID(timeIntervalUUID);

                    smLinkData.add(Pair.of(logicalDivisionUUID, timeIntervalUUID));
                    smLinkData.add(Pair.of(logicalDivisionIDs.get(logicalDivisionChild), timeIntervalUUID));

                    div.setTYPE(PhysicalDivision.TYPE_TRACK);

                    AreaType areaType = new AreaType();
                    areaType.setBETYPE("TIME");

                    MetadataGroup metadataGroup = (MetadataGroup) potentialMetadataGroup;
                    for (Metadata potentialMetadataEntry : metadataGroup.getGroup()) {
                        if (potentialMetadataEntry instanceof MetadataEntry
                                && MdSec.TIMESTAMP.equals(potentialMetadataEntry.getDomain())) {
                            MetadataEntry metadataEntry = (MetadataEntry) potentialMetadataEntry;
                            if (metadataEntry.getAttributes().containsKey(TIME_INTERVAL_POSITION) && metadataEntry
                                    .getAttributes().get(TIME_INTERVAL_POSITION).equals(TIME_INTERVAL_POSITION_END)) {
                                areaType.setEND(metadataEntry.getValue());
                            } else {
                                areaType.setBEGIN(metadataEntry.getValue());
                            }
                        }
                    }

                    Fptr fptr = new Fptr();
                    fptr.setArea(areaType);
                    div.getFptr().add(fptr);
                    parentDiv.getDiv().add(div);
                }
            }
        }

        return parentDiv;
    }
}
