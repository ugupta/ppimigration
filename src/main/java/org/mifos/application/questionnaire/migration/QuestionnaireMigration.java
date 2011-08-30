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

package org.mifos.application.questionnaire.migration;

import static java.lang.String.format;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.mifos.application.questionnaire.migration.mappers.QuestionnaireMigrationMapper;
import org.mifos.customers.ppi.business.PPISurvey;
import org.mifos.customers.ppi.business.PPISurveyInstance;
import org.mifos.customers.surveys.business.Question;
import org.mifos.customers.surveys.business.SurveyInstance;
import org.mifos.customers.surveys.business.SurveyQuestion;
import org.mifos.platform.questionnaire.service.QuestionnaireServiceFacade;
import org.mifos.platform.questionnaire.service.dtos.EventSourceDto;
import org.mifos.platform.questionnaire.service.dtos.QuestionGroupDto;
import org.mifos.platform.questionnaire.service.dtos.QuestionGroupInstanceDto;
import org.mifos.platform.questionnaire.service.dtos.QuestionGroupResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class QuestionnaireMigration {

    private static final Logger logger = LoggerFactory.getLogger(QuestionnaireMigration.class);

    private final QuestionnaireMigrationMapper questionnaireMigrationMapper;

    private final QuestionnaireServiceFacade questionnaireServiceFacade;

    private final SessionFactory sessionFactory;

    private static Integer dateSurveyTakenQuestionId;

    private Session session;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    @Autowired
    public QuestionnaireMigration(QuestionnaireMigrationMapper questionnaireMigrationMapper, QuestionnaireServiceFacade questionnaireServiceFacade, SessionFactory sessionFactory) {
        this.questionnaireMigrationMapper = questionnaireMigrationMapper;
        this.questionnaireServiceFacade = questionnaireServiceFacade;
        this.sessionFactory = sessionFactory;
        session = sessionFactory.openSession();
        session.setFlushMode(FlushMode.COMMIT);
    }

    public List<Integer> migratePPISurveys() {
        List<Integer> questionGroupIds = new ArrayList<Integer>();
        questionGroupIds.addAll(performMigrationOfPPIPPISurvey());
        return questionGroupIds;
    }

    private List<Integer> performMigrationOfPPIPPISurvey() {
        List<Integer> questionGroupIds = new ArrayList<Integer>();
        List<PPISurvey> surveys = retrieveAllPPIPPISurveys();
        if (surveys.size() > 1) {
            logger.warn("There are more than one PPI survey instances in your database");
            for (PPISurvey survey : surveys) {
                logger.warn(survey.getName());
            }
        }
        Integer surveyId = surveys.get(0).getSurveyId();
        fixQuestionTextAndNickName(surveyId);
        fixChoiceText();
        getSession().close();
        getSession().beginTransaction();
        PPISurvey survey = (PPISurvey) getSession().get(PPISurvey.class, surveyId);
        Integer questionGroupId = migratePPISurvey(survey);
        if (questionGroupId != null) {
            questionGroupIds.add(questionGroupId);
        }

        return questionGroupIds;
    }

    private void fixChoiceText() {
        getSession().beginTransaction();
        getSession().createSQLQuery("update question_choices set choice_text='Labourers (agricultural, plantation, other farm), " +
        		"hunters, tobacco preparers and tobacco product makers, and other labourers / ಕೂಲಿಕಾರರು (ಕೃಷಿ/ಕೃಷಿಗೆ ಸ೦ಬ೦ದಿಸಿದ/ಇತರೆ) ಕೂಲಿಕಾರರು ಬೀಡಿ ತಯಾರಕರು " +
        		"/ ಹೊಗೆ ಸೊಪ್ಪು ಉತ್ಪನ್ನ ತಯಾರಕರು / मजदूर, शिकारि, बीडी व तंबाकू उत्पादन अन्य मजदूर' where choice_text='Labourers(farm workers),hunters, tobacco preparers" +
        		"/tobacco product makers,and other labourers / ಕೂಲಿಕಾರರು (ಕೃಷಿ/ಕೃಷಿಗೆ ಸ೦ಬ೦ದಿಸಿದ/ಇತರೆ) ಕೂಲಿಕಾರರು ಬೀಡಿ ತಯಾರಕರು / ಹೊಗೆ ಸೊಪ್ಪು ಉತ್ಪನ್ನ ತಯಾರಕರು / मजदूर, शिकारि, बीडी व तंबाकू उत्पादन अन्य मजदूर';").executeUpdate();
        getSession().createSQLQuery("update question_choices set choice_text='Five or more / ಐದು ಅಥವಾ ಅಧಿಕ / पान्च या उससे अधिक' where choice_text='5 or more / ಐದು ಅಥವಾ ಅಧಿಕ / पान्च या उससे अधिक'").executeUpdate();
        getSession().getTransaction().commit();
        getSession().close();
    }

    private void fixQuestionTextAndNickName(Integer surveyId) {
        getSession().close();
        getSession().beginTransaction();
        PPISurvey survey = (PPISurvey) getSession().get(PPISurvey.class, surveyId);
        for(SurveyQuestion sq: survey.getQuestions()) {
            Question q = sq.getQuestion();
            q.setNickname(getNickName(q));
            q.setQuestionText(getQuestionText(q));
        }
        getSession().getTransaction().commit();
        getSession().close();
    }

    private String getNickName(Question question) {
        if(question.getQuestionText().contains("How many children aged 0 to 17 are in the household?")) {
            return "ppi_india_2008_family_members_0_to_17";
        } else if(question.getQuestionText().contains("What is the household`s principal occupation?")) {
            return "ppi_india_2008_principal_occupation";
        } else if(question.getQuestionText().contains("Is the residence all pacca (burnt bricks, stone")) {
            return "ppi_india_2008_has_pucca_home";
        } else if(question.getQuestionText().contains("What is the household`s primary source of energy for")) {
            return "ppi_india_2008_cooking_fuel";
        } else if(question.getQuestionText().contains("Does the household own a television?")) {
            return "ppi_india_2008_owns_television";
        } else if(question.getQuestionText().contains("Does the household own a bicycle, scooter, or motor")) {
            return "ppi_india_2008_owns_bicycle_scooter_motorcycle";
        } else if(question.getQuestionText().contains("Does the household own a almirah/dressing table?")) {
            return "ppi_india_2008_owns_almirah";
        } else if(question.getQuestionText().contains("Does the household own a sewing machine?")) {
            return "ppi_india_2008_owns_sewing_machine";
        } else if(question.getQuestionText().contains("How many pressure cookers or pressure pans does")) {
            return "ppi_india_2008_pressure_cookers";
        } else if(question.getQuestionText().contains("How many electric fans does the household own?")) {
            return "ppi_india_2008_electric_fans";
        }
        return question.getNickname();
    }

    private String getQuestionText(Question question) {
        if(question.getQuestionText().contains("How many children aged 0 to 17 are in the household?")) {
            return "How many people aged 0 to 17 are in the household? / ನಿಮ್ಮ ಮನೆಯಲ್ಲಿ ೦-೧೭ ವಯಸ್ಸಿನವರು ಎಷ್ಟು ಜನರಿದ್ದಾರೆ? ( ೧೮ ವರ್ಷಕ್ಕಿ೦ತ ಒಳಪಟ್ಟವರು ಎಷ್ಟು ಜನರಿದ್ದಾರೆ?) / परिवार मे कितने सदस्यो कि उम्र ०-१७ साल तक कि है?";
        } else if(question.getQuestionText().contains("What is the household`s principal occupation?")) {
            return "What is the household's principal occupation? / ನಿಮ್ಮ ಕುಟು೦ಬದ ಮುಖ್ಯ ಉದ್ಯೋಗ ಯಾವುದು? / परिवार के आय क मुक्य क्षॆत्र क्या है?";
        } else if(question.getQuestionText().contains("Is the residence all pacca (burnt bricks, stone")) {
            return "Is the residence all pucca (burnt bricks, stone, cement, concrete, jackboard/cement-plastered reeds, timber, tiles, galvanised tin or asbestos cement sheets)? / ನಿಮ್ಮ ಮನೆಯು ಪೂರ್ತಿ ಪಕ್ಕಾ ಮನೆಯೇ? (ಸಿಮೆ೦ಟ್/ಕಲ್ಲು/ಇಟ್ಟಿಗೆ/ಮರ/ಟೈಲ್ಸ್/ಟಿನ್ ಶೀಟ್ಸ್/ಅಸ್ಬೆಸ್ಟೋಸ್ ಸಿಮೆ೦ಟ್ ಶೀಟ್ಸ್/ಇತರೆ) / क्या आपका पूरा घर पक्का है?(ईट/पत्थर/सिमॆंट/कांक्रीट, सिमॆंट जेकबॊर्ड/ सिमेंट-प्लास्टर्ड रीड्स/टैल्स/लकडि /टिन रॊड/अस्बेस्ट्स शीट)";
        } else if(question.getQuestionText().contains("What is the household`s primary source of energy for")) {
            return "What is the household's primary source of energy for cooking? / ನಿಮ್ಮ ಮನೆಯಲ್ಲಿ ಬಳಸುವ ಮೂಲ ಅಡುಗೆ ಇ೦ಧನ ಯಾವುದು? / भॊजन पकाने के लिये परिवार के पास इंधन का प्रमुख स्रॊत क्या है?";
        } else if(question.getQuestionText().contains("Does the household own a television?")) {
            return "Does the household own a television? / ನಿಮ್ಮ ಮನೆಯಲ್ಲಿ ಟೀವಿ ಇದೆಯೇ? / क्या परिवार के पास टेलिविजन है?";
        } else if(question.getQuestionText().contains("Does the household own a bicycle, scooter, or motor")) {
            return "Does the household own a bicycle, scooter, or motor cycle? / ನಿಮ್ಮ ಮನೆಯಲ್ಲಿ ಸೈಕಲ್ / ಸ್ಕೂಟರ್ ಇದೆಯೇ? / क्या परिवार मे अपना सईकिल, स्कूटर अथवा मॊटर सईकिल है?";
        } else if(question.getQuestionText().contains("Does the household own a almirah/dressing table?")) {
            return "Does the household own an almirah/dressing table? / ನಿಮ್ಮ ಮನೆಯಲ್ಲಿ ಬೀರು/ಡ್ರೆಸ್ಸಿ೦ಗ್ ಟೇಬಲ್ ಇದೆಯೇ? / क्या परिवार मे अपना अलमारि या श्रुंगारदान है?";
        } else if(question.getQuestionText().contains("Does the household own a sewing machine?")) {
            return "Does the household own a sewing machine? / ನಿಮ್ಮ ಮನೆಯಲ್ಲಿ ಹೊಲಿಗೆ ಯ೦ತ್ರ ಇದೆಯೇ? / क्या परिवार मे अपना सिलाई मशीन है?";
        } else if(question.getQuestionText().contains("How many pressure cookers or pressure pans does")) {
            return "How many pressure cookers or pressure pans does the household own? / ನಿಮ್ಮ ಮನೆಯಲ್ಲಿ ಎಷ್ಟು ಕುಕ್ಕರ್ ಪ್ರೆಷರ್ ಪ್ಯಾನ್ ಇದೆ? / परिवार मे कितने प्रॆशर कुक्कर है?";
        } else if(question.getQuestionText().contains("How many electric fans does the household own?")) {
            return "How many electric fans does the household own? / ನಿಮ್ಮ ಮನೆಯಲ್ಲಿ ಎಲೆಕ್ಟ್ರಿಕ್ ಫ್ಯಾನ್ ಇದೆಯೇ? / परिवार मे कितने बिजलि के पंखे है?";
        }
        return question.getQuestionText();
    }

    private Integer migratePPISurvey(PPISurvey survey) {
        Integer questionGroupId = null;
        getSession().beginTransaction();
        getSession().setFlushMode(FlushMode.MANUAL);
        try {
            QuestionGroupDto questionGroupDto = mapPPISurveyToQuestionGroupDto(survey);
            questionGroupId = createQuestionGroup(questionGroupDto, survey);
            Integer eventSourceId = getEventSourceId(questionGroupDto);
            if (migratePPISurveyResponses(survey, questionGroupId, eventSourceId)) {
                /* todo - we don't want to remove surveys */
                getSession().getTransaction().commit();
                logger.info(format("Completed migration for survey '%s' with ID %s", survey.getName(), survey.getSurveyId()));
            }
        } catch (Exception e) {
            logger.error(format("Unable to remove survey '%s' with ID %s", survey.getName(), survey.getSurveyId()), e);
        }

        return questionGroupId;
    }

    private Integer getEventSourceId(QuestionGroupDto questionGroupDto) {
        Integer eventSourceId = null;
        if (questionGroupDto != null) {
            EventSourceDto eventSourceDto = questionGroupDto.getEventSourceDtos().get(0);
            eventSourceId = getEventSourceId(eventSourceDto.getEvent(), eventSourceDto.getSource());
        }
        return eventSourceId;
    }

    private Integer getEventSourceId(String event, String source) {
        try {
            return questionnaireServiceFacade.getEventSourceId(event, source);
        } catch (Exception e) {
            logger.error(format("Unable to obtain the event source ID for event %s and source %s'", event, source), e);
        }
        return null;
    }

    private QuestionGroupDto mapPPISurveyToQuestionGroupDto(PPISurvey survey) {
        try {
            return questionnaireMigrationMapper.map(survey);
        } catch (Exception e) {
            logger.error(format("Unable to convert the ppi survey, '%s' with ID, %s to a Question Group", survey.getName(), survey.getSurveyId()), e);
        }
        return null;
    }

    private Integer createQuestionGroup(QuestionGroupDto questionGroupDto, PPISurvey survey) {
        if (questionGroupDto != null) {
            try {
                return questionnaireServiceFacade.createQuestionGroup(questionGroupDto);
            } catch (Exception e) {
                logger.error(format("Unable to convert the ppi survey, '%s' with ID, %s to a Question Group", survey.getName(), survey.getSurveyId()), e);
            }
        }
        return null;
    }

    private static int surveysCount = 0;

    private boolean migratePPISurveyResponses(PPISurvey survey, Integer questionGroupId, Integer eventSourceId) {
        boolean result = false;
        if (questionGroupId != null && eventSourceId != null) {
            result = true;
            List<Integer> surveyInstanceIterator = null;
            try {
                getSession().beginTransaction();
                surveyInstanceIterator = retrieveInstances(survey);
                getSession().close();
                getSession().beginTransaction();
                for (Integer surveyInstanceId : surveyInstanceIterator) {
                    ++surveysCount;
                    if (surveysCount % 1000 == 0) {
                        getSession().getTransaction().commit();
                        getSession().close();
                        getSession().beginTransaction();
                        System.out.printf("%d migrated %d survey instances\n", System.currentTimeMillis(), surveysCount);
                    }

                    PPISurveyInstance surveyInstance = (PPISurveyInstance) getSession().get(SurveyInstance.class, surveyInstanceId);
                    if (saveQuestionGroupInstance(mapToQuestionGroupInstance(questionGroupId, eventSourceId, surveyInstance), surveyInstance)) {
                        /* todo - we don't want to remove surveys */
                    } else {
                        result = false;
                    }
                }
            } catch (Exception e) {
                getSession().getTransaction().rollback();
                logger.error(format("Unable to remove survey instance '%s' (survey id: %d)", survey.getName(), survey.getSurveyId()), e);
                result = false;
            }
        }
        return result;
    }

    private QuestionGroupInstanceDto mapToQuestionGroupInstance(Integer questionGroupId, Integer eventSourceId, PPISurveyInstance surveyInstance) {
        QuestionGroupInstanceDto questionGroupInstanceDto = null;
        try {
            questionGroupInstanceDto = questionnaireMigrationMapper.map(surveyInstance, questionGroupId, eventSourceId);
            addDateSurveyTakenQuestionResponse(questionGroupInstanceDto,questionGroupId,surveyInstance);
        } catch (Exception e) {
            logger.error(format("Unable to migrate a survey instance with ID, %s for the ppi survey", surveyInstance.getInstanceId()), e);
        }
        return questionGroupInstanceDto;
    }

    private void addDateSurveyTakenQuestionResponse(QuestionGroupInstanceDto questionGroupInstanceDto, Integer questionGroupId, PPISurveyInstance surveyInstance) {
        Integer questionId = getDateSurveyTakenQuestionId();
        Integer sectionQuestionId = questionnaireServiceFacade.getSectionQuestionId("PPI India 2008", questionId, questionGroupId);
        QuestionGroupResponseDto questionGroupResponseDto = new QuestionGroupResponseDto();
        questionGroupResponseDto.setResponse(sdf.format((surveyInstance.getDateConducted())));
        questionGroupResponseDto.setSectionQuestionId(sectionQuestionId);
        questionGroupInstanceDto.addQuestionGroupResponseDto(questionGroupResponseDto);

    }

    private boolean saveQuestionGroupInstance(QuestionGroupInstanceDto questionGroupInstanceDto, PPISurveyInstance surveyInstance) {
        if (questionGroupInstanceDto != null) {
            try {
                questionnaireServiceFacade.saveQuestionGroupInstance(questionGroupInstanceDto);
                return true;
            } catch (Exception e) {
                logger.error(format("Unable to migrate a survey instance with ID, %s for the ppi survey", surveyInstance.getInstanceId()), e);
            }
        }
        return false;
    }

    public Session getSession() {
        if (session == null || !session.isOpen() || !session.isConnected()) {
            session = sessionFactory.openSession();
            session.setFlushMode(FlushMode.COMMIT);
        }
        return session;
    }

    public Integer getDateSurveyTakenQuestionId() {
        if (dateSurveyTakenQuestionId == null) {
            dateSurveyTakenQuestionId = (Integer) getSession().createSQLQuery("select question_id from questions where question_text='Date Survey Was Taken'").uniqueResult();
        }
        return dateSurveyTakenQuestionId;
    }

    @SuppressWarnings("unchecked")
    public List<PPISurvey> retrieveAllPPIPPISurveys() {
        Query query = getSession().createQuery("from PPISurvey");
        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<Integer> retrieveInstances(PPISurvey survey) {
        Query query = getSession().createQuery("select instance.instanceId from SurveyInstance as instance where instance.survey.surveyId=:SURVEY_ID");
        query.setParameter("SURVEY_ID", survey.getSurveyId());
        return query.list();
    }

}