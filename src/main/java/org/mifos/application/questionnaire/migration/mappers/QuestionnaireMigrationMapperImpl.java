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

package org.mifos.application.questionnaire.migration.mappers;

import static java.lang.String.format;
import static org.mifos.platform.questionnaire.QuestionnaireConstants.DEFAULT_EVENT_FOR_SURVEYS;
import static org.mifos.platform.questionnaire.QuestionnaireConstants.DEFAULT_ORDER;
import static org.mifos.platform.questionnaire.QuestionnaireConstants.DEFAULT_VERSION;
import static org.mifos.platform.questionnaire.QuestionnaireConstants.MULTI_SELECT_DELIMITER;
import static org.mifos.platform.util.CollectionUtils.asMap;
import static org.mifos.platform.util.MapEntry.makeEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.mifos.customers.surveys.business.Question;
import org.mifos.customers.surveys.business.QuestionChoice;
import org.mifos.customers.surveys.business.Survey;
import org.mifos.customers.surveys.business.SurveyInstance;
import org.mifos.customers.surveys.business.SurveyQuestion;
import org.mifos.customers.surveys.business.SurveyResponse;
import org.mifos.customers.surveys.helpers.AnswerType;
import org.mifos.customers.surveys.helpers.SurveyType;
import org.mifos.platform.questionnaire.QuestionnaireConstants;
import org.mifos.platform.questionnaire.service.QuestionType;
import org.mifos.platform.questionnaire.service.QuestionnaireServiceFacade;
import org.mifos.platform.questionnaire.service.dtos.ChoiceDto;
import org.mifos.platform.questionnaire.service.dtos.EventSourceDto;
import org.mifos.platform.questionnaire.service.dtos.QuestionDto;
import org.mifos.platform.questionnaire.service.dtos.QuestionGroupDto;
import org.mifos.platform.questionnaire.service.dtos.QuestionGroupInstanceDto;
import org.mifos.platform.questionnaire.service.dtos.QuestionGroupResponseDto;
import org.mifos.platform.questionnaire.service.dtos.SectionDto;
import org.springframework.beans.factory.annotation.Autowired;

public class QuestionnaireMigrationMapperImpl implements QuestionnaireMigrationMapper {

    private static final String DEFAULT_SECTION_NAME = "PPI India 2008";
    private static final String DEFAULT_SURVEY_NAME = "PPI India 2008";
    private Map<SurveyType, String> surveyTypeToSourceMap;
    private Map<AnswerType, QuestionType> answerToQuestionType;

    @Autowired
    private QuestionnaireServiceFacade questionnaireServiceFacade;

    public QuestionnaireMigrationMapperImpl() {
        populateSurveyTypeToSourceMappings();
        populateAnswerToQuestionTypeMappings();
    }

    // Intended to be used from unit tests for injecting mocks
    public QuestionnaireMigrationMapperImpl(QuestionnaireServiceFacade questionnaireServiceFacade) {
        this.questionnaireServiceFacade = questionnaireServiceFacade;
    }

    @Override
    public QuestionGroupDto map(Survey survey) {
        QuestionGroupDto questionGroupDto = new QuestionGroupDto();
        questionGroupDto.setTitle(DEFAULT_SURVEY_NAME);
        questionGroupDto.setEditable(false);
        questionGroupDto.setPpi(true);
        questionGroupDto.setActive(survey.getState() == 1);
        questionGroupDto.setEventSourceDtos(Arrays.asList(mapEventSourceForSurvey(survey)));
        questionGroupDto.addSection(mapToSectionForSurvey(survey.getQuestions()));
        return questionGroupDto;
    }

    @Override
    public QuestionGroupInstanceDto map(SurveyInstance surveyInstance, Integer questionGroupId, Integer eventSourceId) {
        QuestionGroupInstanceDto questionGroupInstanceDto = new QuestionGroupInstanceDto();
        questionGroupInstanceDto.setDateConducted(surveyInstance.getDateConducted());
        questionGroupInstanceDto.setCompleted(surveyInstance.getCompletedStatus());
        questionGroupInstanceDto.setCreatorId(surveyInstance.getCreator());
        questionGroupInstanceDto.setEventSourceId(eventSourceId);
        questionGroupInstanceDto.setEntityId(mapToEntityId(surveyInstance));
        questionGroupInstanceDto.setQuestionGroupId(questionGroupId);
        questionGroupInstanceDto.setVersion(DEFAULT_VERSION);
        questionGroupInstanceDto.setQuestionGroupResponseDtos(mapToQuestionGroupResponseDtos(surveyInstance,
                questionGroupId));
        return questionGroupInstanceDto;
    }

    private List<QuestionGroupResponseDto> mapToQuestionGroupResponseDtos(SurveyInstance surveyInstance,
            Integer questionGroupId) {
        List<QuestionGroupResponseDto> questionGroupResponseDtos = new ArrayList<QuestionGroupResponseDto>();
        for (SurveyResponse surveyResponse : surveyInstance.getSurveyResponses()) {
            if (surveyResponse.getQuestion().getAnswerTypeAsEnum() == AnswerType.MULTISELECT) {
                questionGroupResponseDtos
                        .addAll(mapToMultiSelectQuestionGroupResponses(questionGroupId, surveyResponse));
            } else {
                questionGroupResponseDtos.add(mapToQuestionGroupResponse(questionGroupId, surveyResponse));
            }
        }
        return questionGroupResponseDtos;
    }

    private List<QuestionGroupResponseDto> mapToMultiSelectQuestionGroupResponses(Integer questionGroupId,
            SurveyResponse surveyResponse) {
        List<QuestionGroupResponseDto> questionGroupResponseDtos = new ArrayList<QuestionGroupResponseDto>();
        String multiSelectValue = surveyResponse.getMultiSelectValue();
        if (StringUtils.isNotEmpty(multiSelectValue)) {
            Map<Integer, QuestionChoice> choiceLookup = getChoiceLookup(surveyResponse);
            Integer questionId = surveyResponse.getQuestion().getQuestionId();
            Integer sectionQuestionId = getSectionQuestionId(questionGroupId, questionId);
            String[] answers = StringUtils.split(multiSelectValue, MULTI_SELECT_DELIMITER);
            for (int ansIndex = 0; ansIndex < answers.length; ansIndex++) {
                if (isChoiceSelected(answers[ansIndex])) {
                    String answer = choiceLookup.get(ansIndex).getChoiceText();
                    questionGroupResponseDtos.add(mapToQuestionGroupResponse(sectionQuestionId, answer));
                }
            }
        }
        return questionGroupResponseDtos;
    }

    private boolean isChoiceSelected(String answer) {
        return StringUtils.isNotEmpty(answer) && QuestionnaireConstants.CHOICE_SELECTED.equals(answer);
    }

    private Map<Integer, QuestionChoice> getChoiceLookup(SurveyResponse surveyResponse) {
        Map<Integer, QuestionChoice> questionChoiceLookup = new HashMap<Integer, QuestionChoice>();
        for (QuestionChoice questionChoice : surveyResponse.getSurveyQuestion().getQuestion().getChoices()) {
            questionChoiceLookup.put(questionChoice.getChoiceOrder(), questionChoice);
        }
        return questionChoiceLookup;
    }

    private QuestionGroupResponseDto mapToQuestionGroupResponse(Integer sectionQuestionId, String answer) {
        QuestionGroupResponseDto questionGroupResponseDto = new QuestionGroupResponseDto();
        questionGroupResponseDto.setResponse(answer);
        questionGroupResponseDto.setSectionQuestionId(sectionQuestionId);
        return questionGroupResponseDto;
    }

    private QuestionGroupResponseDto mapToQuestionGroupResponse(Integer questionGroupId, SurveyResponse surveyResponse) {
        Integer questionId = surveyResponse.getQuestion().getQuestionId();
        Integer sectionQuestionId = getSectionQuestionId(questionGroupId, questionId);
        return mapToQuestionGroupResponse(sectionQuestionId, surveyResponse.toString());
    }

    private Integer getSectionQuestionId(Integer questionGroupId, Integer questionId) {
        return questionnaireServiceFacade.getSectionQuestionId(DEFAULT_SECTION_NAME, questionId, questionGroupId);
    }

    private Integer mapToEntityId(SurveyInstance surveyInstance) {
        Integer result = 0;
        if (surveyInstance.isForCustomer()) {
            result = surveyInstance.getCustomer();
        } else if (surveyInstance.isForAccount()) {
            result = surveyInstance.getAccount();
        }
        return result;
    }

    private SectionDto mapToSectionForSurvey(List<SurveyQuestion> questions) {
        SectionDto sectionDto = getDefaultSection();
        for (SurveyQuestion question : questions) {
            sectionDto.addQuestion(mapToQuestionDto(question));
        }
        addDateSurveyConductedQuestion(sectionDto);
        return sectionDto;
    }

    private void addDateSurveyConductedQuestion(SectionDto sectionDto) {
        QuestionDto questionDto = new QuestionDto();
        questionDto.setText("Date Survey Was Taken");
        questionDto.setMandatory(true);
        questionDto.setActive(true);
        questionDto.setNickname("ppi_india_2008_survey_date");
        questionDto.setOrder(0);
        questionDto.setType(answerToQuestionType.get(AnswerType.DATE));
        sectionDto.addQuestion(questionDto);
    }

    private QuestionDto mapToQuestionDto(SurveyQuestion surveyQuestion) {
        QuestionDto questionDto = new QuestionDto();
        Question question = surveyQuestion.getQuestion();
        questionDto.setText(question.getQuestionText());
        questionDto.setNickname(question.getNickname());
        questionDto.setMandatory(surveyQuestion.getMandatory() == 1);
        questionDto.setActive(question.getQuestionState() == 1);
        questionDto.setOrder(surveyQuestion.getOrder() + 1);
        AnswerType answerType = question.getAnswerTypeAsEnum();
        questionDto.setType(answerToQuestionType.get(answerType));
        if (answerType == AnswerType.NUMBER) {
            mapNumberQuestion(questionDto, question);
        } else if (answerType == AnswerType.SINGLESELECT || answerType == AnswerType.MULTISELECT
                || answerType == AnswerType.CHOICE) {
            mapChoiceBasedQuestion(questionDto, question.getChoices());
        }
        return questionDto;
    }



    private void mapChoiceBasedQuestion(QuestionDto questionDto, List<QuestionChoice> questionChoices) {
        List<ChoiceDto> choices = new ArrayList<ChoiceDto>();
        for (int i = 0, choicesSize = questionChoices.size(); i < choicesSize; i++) {
            QuestionChoice questionChoice = questionChoices.get(i);
            choices.add(mapToChoiceDto(i, questionChoice));
        }
        questionDto.setChoices(choices);
    }

    private ChoiceDto mapToChoiceDto(int i, QuestionChoice questionChoice) {
        ChoiceDto choiceDto = new ChoiceDto();
        choiceDto.setOrder(i);
        choiceDto.setValue(questionChoice.getChoiceText());
        return choiceDto;
    }

    private void mapNumberQuestion(QuestionDto questionDto, Question question) {
        questionDto.setMinValue(question.getNumericMin());
        questionDto.setMaxValue(question.getNumericMax());
    }

    private EventSourceDto mapEventSourceForSurvey(Survey survey) {
        SurveyType surveyType = survey.getAppliesToAsEnum();
        String event = DEFAULT_EVENT_FOR_SURVEYS;
        String source = surveyTypeToSourceMap.get(surveyType);
        return new EventSourceDto(event, source, getEventSourceDescription(event, source));
    }

    private String getEventSourceDescription(String event, String source) {
        return format("%s %s", event, source);
    }

    private SectionDto getDefaultSection() {
        SectionDto sectionDto = new SectionDto();
        sectionDto.setName(DEFAULT_SECTION_NAME);
        sectionDto.setOrder(DEFAULT_ORDER);
        return sectionDto;
    }

    @SuppressWarnings("unchecked")
    private void populateSurveyTypeToSourceMappings() {
        surveyTypeToSourceMap = asMap(
                makeEntry(SurveyType.CLIENT, "Client"),
                makeEntry(SurveyType.GROUP, "Group"),
                makeEntry(SurveyType.CENTER, "Center"),
                makeEntry(SurveyType.LOAN, "Loan"),
                makeEntry(SurveyType.SAVINGS, "Savings"),
                makeEntry(SurveyType.ALL, "All")
        );
    }

    @SuppressWarnings("unchecked")
    private void populateAnswerToQuestionTypeMappings() {
        answerToQuestionType = asMap(makeEntry(AnswerType.INVALID, QuestionType.INVALID),
                makeEntry(AnswerType.FREETEXT, QuestionType.FREETEXT),
                makeEntry(AnswerType.DATE, QuestionType.DATE),
                makeEntry(AnswerType.NUMBER, QuestionType.NUMERIC),
                makeEntry(AnswerType.SINGLESELECT, QuestionType.SINGLE_SELECT),
                makeEntry(AnswerType.CHOICE, QuestionType.SINGLE_SELECT),
                makeEntry(AnswerType.MULTISELECT, QuestionType.MULTI_SELECT));
    }

}