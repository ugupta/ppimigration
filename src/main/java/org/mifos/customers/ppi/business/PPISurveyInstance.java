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

package org.mifos.customers.ppi.business;

import org.mifos.customers.surveys.business.SurveyInstance;

public class PPISurveyInstance extends SurveyInstance {

    private double bottomHalfBelowPovertyLinePercent;
    private double topHalfBelowPovertyLinePercent;
    private double belowPovertyLinePercent;
    private double abovePovertyLinePercent;
    private int score;

    public double getBottomHalfBelowPovertyLinePercent() {
        return bottomHalfBelowPovertyLinePercent;
    }

    public double getTopHalfBelowPovertyLinePercent() {
        return topHalfBelowPovertyLinePercent;
    }

    public double getBelowPovertyLine() {
        return belowPovertyLinePercent;
    }

    public double getAbovePovertyLinePercent() {
        return abovePovertyLinePercent;
    }

    public int getScore() {
        return score;
    }

    private void setBottomHalfBelowPovertyLinePercent(double bottomHalfBelowPovertyLinePercent) {
        this.bottomHalfBelowPovertyLinePercent = bottomHalfBelowPovertyLinePercent;
    }

    private void setTopHalfBelowPovertyLinePercent(double topHalfBelowPovertyLinePercent) {
        this.topHalfBelowPovertyLinePercent = topHalfBelowPovertyLinePercent;
    }

    private void setBelowPovertyLinePercent(double belowPovertyLinePercent) {
        this.belowPovertyLinePercent = belowPovertyLinePercent;
    }

    private void setAbovePovertyLinePercent(double abovePovertyLinePercent) {
        this.abovePovertyLinePercent = abovePovertyLinePercent;
    }

    private void setScore(int score) {
        this.score = score;
    }
}
