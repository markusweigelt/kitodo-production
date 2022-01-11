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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.MdSec;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.MetadataGroup;
import org.kitodo.api.dataeditor.rulesetmanagement.FunctionalMetadata;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.api.dataformat.mets.KitodoUUID;
import org.kitodo.dataformat.metskitodo.AreaType;
import org.kitodo.dataformat.metskitodo.DivType;
import org.kitodo.dataformat.metskitodo.DivType.Fptr;

public class TimeIntervalXmlElementAccess {

    public static final String TIME_INTERVAL_POSITION = "timeIntervalPosition";
    public static final String TIME_INTERVAL_POSITION_END = "end";

    static Collection<Metadata> readMetadata(DivType div, String potentialLogicalDivisionID,
            RulesetManagementInterface rulesetManagement) {
        Collection<Metadata> groups = new HashSet<>();
       for (DivType childDiv : div.getDiv()) {
            if (PhysicalDivision.TYPE_TRACK.equals(childDiv.getTYPE())
                    && childDiv.getID().equals(potentialLogicalDivisionID)) {
                List<Fptr> fptrs = childDiv.getFptr();
                if (fptrs.size() == 1) {
                    addMetadataGroup(groups, rulesetManagement, fptrs.get(0).getArea());
                }
            }
        }
        return groups;
    }

    private static void addMetadataGroup(Collection<Metadata> groups, RulesetManagementInterface rulesetManagement,
            AreaType areaType) {
        Optional<String> keyOptional = rulesetManagement.getFunctionalKeys(FunctionalMetadata.TIME_INTERVAL).stream()
                .findFirst();
        if (keyOptional.isPresent()) {
            MetadataGroup group = new MetadataGroup();
            group.setDomain(MdSec.TIME_INTERVAL);
            group.setKey(keyOptional.get());
            Collection<Metadata> groupMetadata = new HashSet<>();
            addMetadataEntry(groupMetadata, rulesetManagement, FunctionalMetadata.TIMESTAMP_BEGIN, areaType.getBEGIN());
            addMetadataEntry(groupMetadata, rulesetManagement, FunctionalMetadata.TIMESTAMP_END, areaType.getEND());
            group.setGroup(groupMetadata);
            groups.add(group);
        }
    }

    private static void addMetadataEntry(Collection<Metadata> metadata, RulesetManagementInterface rulesetManagement,
            FunctionalMetadata functionalMetadata, String value) {
        if (Objects.nonNull(value)) {
            Optional<String> keyOptional = rulesetManagement.getFunctionalKeys(functionalMetadata).stream().findFirst();
            if (keyOptional.isPresent()) {
                MetadataEntry metadataEntry = new MetadataEntry();
                metadataEntry.setDomain(MdSec.TIMESTAMP);
                metadataEntry.setKey(StringUtils.substringAfterLast(keyOptional.get(), "@"));
                metadataEntry.setValue(value);
                metadata.add(metadataEntry);
            }
        }
    }

    static DivType toDiv(LogicalDivision logicalDivision, Map<LogicalDivision, String> logicalDivisionIDs,
            LinkedList<Pair<String, String>> smLinkData) {
        DivType parentDiv = new DivType();
        parentDiv.setID(KitodoUUID.randomUUID());

        String logicalDivisionUUID = logicalDivisionIDs.get(logicalDivision);

        for (LogicalDivision logicalDivisionChild : logicalDivision.getChildren()) {
            for (Metadata potentialMetadataGroup : logicalDivisionChild.getMetadata()) {
                if (potentialMetadataGroup instanceof MetadataGroup
                        && MdSec.TIME_INTERVAL.equals(potentialMetadataGroup.getDomain())) {
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
                            if (Objects.nonNull(metadataEntry.getAttributes())
                                    && metadataEntry.getAttributes().containsKey(TIME_INTERVAL_POSITION)
                                    && metadataEntry.getAttributes().get(TIME_INTERVAL_POSITION)
                                            .equals(TIME_INTERVAL_POSITION_END)) {
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
