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

import java.util.Date;
import java.util.List;

import org.mifos.customers.surveys.helpers.SurveyState;
import org.mifos.customers.surveys.helpers.SurveyType;

public class Survey {

	private int surveyId;
    private String name;
    private SurveyType appliesTo;
    private Date dateOfCreation;
    private SurveyState state;
    private List<SurveyQuestion> questions;

    // for hibernate and jsp
    public String getAppliesTo() {
        return appliesTo.getValue();
    }

    /**
     * implements a dispatch pattern so that a client will get the correct class
     * of survey without having to invoke instanceOf(). Since this class
     * represents custom surveys, the client gets a (custom) SurveyInstance
     * instance.
     */
    public SurveyInstance createSurveyInstance() {
        return new SurveyInstance();
    }

    public SurveyType getAppliesToAsEnum() {
        return appliesTo;
    }

    public void setAppliesTo(String appliesTo) {
        this.appliesTo = SurveyType.fromString(appliesTo);
    }

    public void setAppliesTo(SurveyType appliesTo) {
        this.appliesTo = appliesTo;
    }

    public Date getDateOfCreation() {
        return dateOfCreation;
    }

    public void setDateOfCreation(Date dateofCreation) {
        this.dateOfCreation = dateofCreation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SurveyState getStateAsEnum() {
        return state;
    }

    // Called from jsp as well as hibernate
    public int getState() {
        return state.getValue();
    }

    public void setState(SurveyState state) {
        this.state = state;
    }

    public void setState(int state) {
        this.state = SurveyState.fromInt(state);
    }

    public int getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(int surveyId) {
        this.surveyId = surveyId;
    }

    public List<SurveyQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<SurveyQuestion> questions) {
        this.questions = questions;
    }

    public Question getQuestion(int i) {
        return getQuestions().get(i).getQuestion();
    }

    public SurveyQuestion getSurveyQuestionById(int id) {
        for (SurveyQuestion surveyQuestion : getQuestions()) {
            if (surveyQuestion.getSurveyQuestionId() == id) {
                return surveyQuestion;
            }
        }
        throw new IllegalArgumentException("Survey does not contain a question with id: " + id);
    }

    public Question getQuestionById(int id) {
        for (SurveyQuestion surveyQuestion : this.getQuestions()) {
            Question question = surveyQuestion.getQuestion();
            if (question.getQuestionId() == id) {
                return question;
            }
        }
        return null;
    }

    public String getQuestionText(int i) {
        return getQuestion(i).getQuestionText();
    }

    @Override
    public String toString() {
        return "<Survey " + getName() + " " + appliesTo.getValue() + ">";
    }

}
