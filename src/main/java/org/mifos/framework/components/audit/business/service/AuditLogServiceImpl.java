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

package org.mifos.framework.components.audit.business.service;

import org.mifos.platform.questionnaire.AuditLogService;
import org.mifos.platform.questionnaire.service.QuestionGroupDetail;

public class AuditLogServiceImpl implements AuditLogService {
    public static final String CREATE = "create";

    @Override
    public void addAuditLogRegistry(QuestionGroupDetail questionGroupDetail,
            QuestionGroupDetail oldQuestionGroupDetail, int creatorId, int entityId, String source, String event) {
        //do nothing, we don't need audit logging for migration
    }

}
