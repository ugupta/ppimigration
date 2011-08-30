/*
 * Copyright (c) 2005-2010 Grameen Foundation USA
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 *  See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 *  explanation of the license and how it is applied.
 */

package org.mifos.ppimigration;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.mifos.application.questionnaire.migration.QuestionnaireMigration;
import org.mifos.framework.util.DateTimeService;
import org.springframework.beans.factory.annotation.Autowired;

public class MigrationScript {

	@Autowired
    private QuestionnaireMigration questionnaireMigration;

    public MigrationScript() {
        super();
    }

    // Should only be used from tests to inject mocks
    public MigrationScript(QuestionnaireMigration questionnaireMigration) {
        this.questionnaireMigration = questionnaireMigration;
    }

    public void upgrade() throws IOException, SQLException {
        long time1 = new DateTimeService().getCurrentDateTime().getMillis();
        logMessage(" - migrating PPI...");
        questionnaireMigration.migratePPISurveys();
        logMessage("    - took " + (new DateTimeService().getCurrentDateTime().getMillis() - time1) + " msec");
    }


    private void logMessage(String finalMessage) {
        System.out.println(finalMessage);
        Logger.getLogger(getClass()).info(finalMessage);
    }
}
