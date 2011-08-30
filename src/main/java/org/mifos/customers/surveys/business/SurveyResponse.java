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

import org.mifos.customers.surveys.helpers.AnswerType;

public class SurveyResponse implements Comparable<SurveyResponse> {
    private int responseId;

    private SurveyInstance instance;

    private SurveyQuestion surveyQuestion;

    private String freetextValue;

    private Date dateValue;

    private QuestionChoice choiceValue;

    private String multiSelectValue;

    private Double numberValue;

    public Question getQuestion() {
        if (getSurveyQuestion() == null) {
            return null;
        }
        return getSurveyQuestion().getQuestion();
    }

    public SurveyInstance getInstance() {
        return instance;
    }

    public int getResponseId() {
        return responseId;
    }

    public QuestionChoice getChoiceValue() {
        return choiceValue;
    }

    public String getMultiSelectValue() {
        return multiSelectValue;
    }

    public Date getDateValue() {
        return dateValue;
    }

    public String getFreetextValue() {
        return freetextValue;
    }

    public Double getNumberValue() {
        return numberValue;
    }

    @Override
    public String toString() {
        AnswerType answerType = getQuestion().getAnswerTypeAsEnum();

        if (answerType == AnswerType.FREETEXT) {
            return getFreetextValue();
        }

        else if (answerType == AnswerType.NUMBER) {
            return Double.toString(numberValue);
        }

        else if (answerType == AnswerType.DATE) {
            return getDateValue().toString();
        }

        else if (answerType == AnswerType.CHOICE || answerType == AnswerType.SINGLESELECT) {
            return getChoiceValue().getChoiceText();
        }

        else if (answerType == AnswerType.MULTISELECT) {
            return multiSelectValue;
        }

        else {
            return null;
        }
    }

    public SurveyQuestion getSurveyQuestion() {
        return surveyQuestion;
    }

    @Override
    public final boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!(o instanceof SurveyResponse)) {
            return false;
        }

        SurveyResponse response = (SurveyResponse) o;
        return response.getResponseId() == responseId;
    }

    @Override
    public int hashCode() {
        return new Integer(responseId).hashCode();
    }

    @Override
    public int compareTo(SurveyResponse o) {
        return getSurveyQuestion().compareTo(o.getSurveyQuestion());
    }

    private void setResponseId(int responseId) {
        this.responseId = responseId;
    }

    private void setInstance(SurveyInstance instance) {
        this.instance = instance;
    }

    private void setSurveyQuestion(SurveyQuestion surveyQuestion) {
        this.surveyQuestion = surveyQuestion;
    }

    private void setFreetextValue(String freetextValue) {
        this.freetextValue = freetextValue;
    }

    private void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    private void setChoiceValue(QuestionChoice choiceValue) {
        this.choiceValue = choiceValue;
    }

    private void setMultiSelectValue(String multiSelectValue) {
        this.multiSelectValue = multiSelectValue;
    }

    private void setNumberValue(Double numberValue) {
        this.numberValue = numberValue;
    }
}
