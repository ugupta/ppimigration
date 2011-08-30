/*
 * Copyright (c) 2005-2010 Grameen Foundation USA
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 * explanation of the license and how it is applied.
 */

package org.mifos.customers.surveys.business;

import org.mifos.customers.surveys.helpers.InstanceStatus;
import org.mifos.customers.surveys.helpers.SurveyType;

import java.util.Date;
import java.util.Set;

public class SurveyInstance {

    private int instanceId;

    private Survey survey;

    private Set<SurveyResponse> surveyResponses;

    private Integer customer;

    private Integer account;

    private Integer officer;

    private Integer creator;

    private Date dateConducted;

    private InstanceStatus completedStatus;

    public SurveyInstance() {
        completedStatus = InstanceStatus.INCOMPLETE;
    }

    public int getCompletedStatus() {
        return completedStatus.getValue();
    }

    public InstanceStatus getCompletedStatusAsEnum() {
        return completedStatus;
    }

    public void setCompletedStatus(int completedStatus) {
        this.completedStatus = InstanceStatus.fromInt(completedStatus);
    }

    public void setCompletedStatus(InstanceStatus completedStatus) {
        this.completedStatus = completedStatus;
    }

    public Date getDateConducted() {
        return dateConducted;
    }

    public void setDateConducted(Date dateConducted) {
        this.dateConducted = dateConducted;
    }

    public int getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(int instanceId) {
        this.instanceId = instanceId;
    }

    public Integer getOfficer() {
        return officer;
    }

    public void setOfficer(Integer officer) {
        this.officer = officer;
    }

    public Survey getSurvey() {
        return survey;
    }

    public void setSurvey(Survey survey) {
        this.survey = survey;
    }

    public Integer getCustomer() {
        return customer;
    }

    /*
     * note that a survey instance must be associated with either a client or an
     * account, not both... we could include a check against the survey type
     * here, but that would cause needless errors when you set the
     * client/account before the survey
     */
    public void setCustomer(Integer customer) {
        this.customer = customer;
        this.account = null;
    }

    public Integer getAccount() {
        return account;
    }

    public void setAccount(Integer account) {
        this.account = account;
        this.customer = null;
    }

    public Integer getCreator() {
        return creator;
    }

    public void setCreator(Integer creator) {
        this.creator = creator;
    }

    public void setSurveyResponses(Set<SurveyResponse> surveyResponses) {
        this.surveyResponses = surveyResponses;
    }

    public Set<SurveyResponse> getSurveyResponses() {
        return surveyResponses;
    }

    public boolean isForCustomer() {
        SurveyType appliesTo = survey.getAppliesToAsEnum();
        return appliesTo == SurveyType.CLIENT || appliesTo == SurveyType.GROUP || appliesTo == SurveyType.CENTER;
    }

    public boolean isForAccount() {
        SurveyType appliesTo = survey.getAppliesToAsEnum();
        return appliesTo == SurveyType.LOAN || appliesTo == SurveyType.SAVINGS;
    }
}
