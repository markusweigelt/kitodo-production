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

package org.kitodo.production.services.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.kitodo.data.database.beans.DataEditorSetting;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.DataEditorSettingDAO;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.primefaces.model.SortMeta;

public class DataEditorSettingService extends SearchDatabaseService<DataEditorSetting, DataEditorSettingDAO> {

    private static volatile DataEditorSettingService instance = null;

    /**
     * Constructor.
     */
    private DataEditorSettingService() {
        super(new DataEditorSettingDAO());
    }

    /**
     * Return signleton variable of type DataEditorSettingService.
     *
     * @return unique instance of DataEditorSettingService
     */
    public static DataEditorSettingService getInstance() {
        DataEditorSettingService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (DataEditorSettingService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new DataEditorSettingService();
                    instance = localReference;
                }
            }
        }
        return localReference;

    }

    @Override
    public List loadData(int first, int pageSize, Map<String, SortMeta> sortMetaMap, Map filters) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM DataEditorSetting");
    }

    @Override
    public Long countResults(Map filters) throws DAOException {
        return countDatabaseRows();
    }

    private List<DataEditorSetting> getByUserAndTask(int userId, int taskId) {
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("userId", userId);
        parameterMap.put("taskId", taskId);
        return getByQuery("FROM DataEditorSetting WHERE user_id = :userId AND task_id = :taskId ORDER BY id ASC", parameterMap);
    }

    /**
     * Load DataEditorSetting from database or return null if no entry matches the specified ids.
     * @param userId id of the user
     * @param taskId id of the corresponding template task for the task that is currently edited
     * @return settings for the data editor
     */
    public DataEditorSetting loadDataEditorSetting(int userId, int taskId) {
        List<DataEditorSetting> results = getByUserAndTask(userId, taskId);
        if (Objects.nonNull(results) && !results.isEmpty()) {
            return results.get(0);
        }
        return null;
    }


}
