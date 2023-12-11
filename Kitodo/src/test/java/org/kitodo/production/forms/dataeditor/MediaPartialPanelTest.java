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

package org.kitodo.production.forms.dataeditor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.MediaPartialView;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.production.helper.metadata.MediaPartialHelper;

public class MediaPartialPanelTest {

    MediaPartialsPanel mediaPartialsPanel;

    /**
     * Initialize test function.
     */
    @BeforeEach
    public void initTest() {
        DataEditorForm dataEditorForm = mock(DataEditorForm.class);
        mediaPartialsPanel = spy(new MediaPartialsPanel(dataEditorForm));
    }

    /**
     * Test generation of extent and sorting of media partials.
     */
    @Test
    public void testGenerateExtentAndSortMediaPartials() {
        List<LogicalDivision> logicalDivisions = new ArrayList<>();
        logicalDivisions.add(getLogicalDivisionWithMediaPartial("Lorem ipsum", "00:00:45.001"));
        logicalDivisions.add(getLogicalDivisionWithMediaPartial("Lorem ipsum", "00:00:00.002"));
        logicalDivisions.add(getLogicalDivisionWithMediaPartial("Lorem ipsum", "00:00:55.894"));
        logicalDivisions.add(getLogicalDivisionWithMediaPartial("Lorem ipsum", "00:00:35.123"));

        // one minute media duration
        MediaPartialHelper.calculateExtentAndSortMediaPartials(logicalDivisions, 60000L);

        Assert.assertEquals("00:00:00.002", ((MediaPartialView) logicalDivisions.get(0).getViews().get(0)).getBegin());
        Assert.assertEquals("00:00:35.121", ((MediaPartialView) logicalDivisions.get(0).getViews().get(0)).getExtent());
        Assert.assertEquals("00:00:35.123", ((MediaPartialView) logicalDivisions.get(1).getViews().get(0)).getBegin());
        Assert.assertEquals("00:00:09.878", ((MediaPartialView) logicalDivisions.get(1).getViews().get(0)).getExtent());
        Assert.assertEquals("00:00:45.001", ((MediaPartialView) logicalDivisions.get(2).getViews().get(0)).getBegin());
        Assert.assertEquals("00:00:10.893", ((MediaPartialView) logicalDivisions.get(2).getViews().get(0)).getExtent());
        Assert.assertEquals("00:00:55.894", ((MediaPartialView) logicalDivisions.get(3).getViews().get(0)).getBegin());
        Assert.assertEquals("00:00:04.106", ((MediaPartialView) logicalDivisions.get(3).getViews().get(0)).getExtent());
    }

    /**
     * Test media duration validation.
     */
    @Test
    public void testMediaDurationValidation() {
        assertEquals("mediaPartialFormMediaDurationEmpty", mediaPartialsPanel.validateMediaDuration());
        when(mediaPartialsPanel.getMediaDuration()).thenReturn("123456");
        assertEquals("mediaPartialFormMediaDurationWrongTimeFormat", mediaPartialsPanel.validateMediaDuration());
        when(mediaPartialsPanel.getMediaDuration()).thenReturn("00:01:00.000");
        Assert.assertNull(mediaPartialsPanel.validateMediaDuration());
    }

    /**
     * Test converting of formatted time and milliseconds.
     */
    @Test
    public void testConverting() {
        assertEquals(Long.valueOf(3661012L), MediaPartialHelper.convertFormattedTimeToMilliseconds("01:01:01.012"));
        assertEquals("01:01:01.120", MediaPartialHelper.convertMillisecondsToFormattedTime(3661120L));
    }

    private static LogicalDivision getLogicalDivisionWithMediaPartial(String label, String begin) {
        LogicalDivision logicalDivision = new LogicalDivision();
        logicalDivision.setLabel(label);
        PhysicalDivision physicalDivision = new PhysicalDivision();
        MediaPartialView mediaPartialView = new MediaPartialView(begin);
        physicalDivision.setMediaPartialView(mediaPartialView);
        logicalDivision.getViews().add(mediaPartialView);
        return logicalDivision;
    }
}
