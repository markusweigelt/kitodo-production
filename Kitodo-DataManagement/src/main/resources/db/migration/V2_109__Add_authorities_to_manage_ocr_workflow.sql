--
-- (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
--
-- This file is part of the Kitodo project.
--
-- It is licensed under GNU General Public License version 3 or later.
--
-- For the full copyright and license information, please read the
-- GPL3-License.txt file that was distributed with this source code.
--

-- Add authorities to manage ocr workflows
INSERT IGNORE INTO authority (title) VALUES ('addOCRWorkflow_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('editOCRWorkflow_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('deleteOCRWorkflow_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('viewOCRWorkflow_clientAssignable');
