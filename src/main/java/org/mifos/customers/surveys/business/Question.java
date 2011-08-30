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

import java.util.LinkedList;
import java.util.List;

import org.mifos.customers.surveys.SurveysConstants;
import org.mifos.customers.surveys.helpers.AnswerType;
import org.mifos.customers.surveys.helpers.QuestionState;

public class Question implements Comparable<Question> {

	private int questionId;

    private AnswerType answerType;

    private QuestionState questionState;

    private String questionText;

    private String nickname;

    private Integer numericMin;

    private Integer numericMax;

    private List<QuestionChoice> choices = new LinkedList<QuestionChoice>();

    public Integer getNumericMax() {
        return numericMax;
    }

    public void setNumericMax(Integer numericMax) {
        this.numericMax = numericMax;
    }

    public Integer getNumericMin() {
        return numericMin;
    }

    public void setNumericMin(Integer numericMin) {
        this.numericMin = numericMin;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public QuestionState getQuestionStateAsEnum() {
        return questionState;
    }

    public int getQuestionState() {
        return questionState.getValue();
    }

    public void setQuestionState(int state) {
        this.questionState = QuestionState.fromInt(state);
    }

    public void setQuestionState(QuestionState questionState) {
        this.questionState = questionState;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public AnswerType getAnswerTypeAsEnum() {
        return answerType;
    }

    public int getAnswerType() {
        return answerType.getValue();
    }

    public void setAnswerType(AnswerType answerType) {
        this.answerType = answerType;
    }

    public void setAnswerType(int answerType) {
        this.answerType = AnswerType.fromInt(answerType);
    }

    @Override
    public String toString() {
        return "<Question " + questionId + " \"" + questionText + "\">";
    }

    public List<QuestionChoice> getChoices() {
        return choices;
    }

    public void setChoices(List<QuestionChoice> choices) {
        this.choices = choices;
    }

    public void addChoice(QuestionChoice choice) {
        getChoices().add(choice);
    }

    public int getQuestionType() {
        if (answerType == AnswerType.CHOICE) {
            return SurveysConstants.QUESTION_TYPE_PPI;
        }
        return SurveysConstants.QUESTION_TYPE_GENERAL;
    }

    @Override
    public final boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!(o instanceof Question)) {
            return false;
        }

        Question question = (Question) o;
        return question.getQuestionId() == getQuestionId();
    }

    @Override
    public int hashCode() {
        return new Integer(getQuestionId()).hashCode();
    }

    @Override
    public int compareTo(Question other) {
        return getQuestionText().compareTo(other.getQuestionText());
    }

}
