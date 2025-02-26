/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Ioannis Moutsatsos, Bruno P. Kinoshita
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.biouno.unochoice.issue34818;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import hudson.model.Descriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.biouno.unochoice.CascadeChoiceParameter;
import org.biouno.unochoice.ChoiceParameter;
import org.biouno.unochoice.model.GroovyScript;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.scriptsecurity.scripts.languages.GroovyLanguage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.slaves.EnvironmentVariablesNodeProperty;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Test that scripts can access global node properties.
 *
 * @since 1.5.x
 */
@Issue("JENKINS-34818")
@WithJenkins
class TestGlobalNodePropertiesScript {

    private JenkinsRule j;

    private static final String SCRIPT = "return ['a', 'b', \"$NODE_TIME\"]";
    private static final String FALLBACK_SCRIPT = "return ['EMPTY!']";

    @BeforeEach
    void setUp(JenkinsRule j) {
        this.j = j;
        ScriptApproval.get().preapprove(SCRIPT, GroovyLanguage.get());
        ScriptApproval.get().preapprove(FALLBACK_SCRIPT, GroovyLanguage.get());
    }

    @Test
    void testScriptAccessingGlobalProperties() throws Descriptor.FormException {
        Map<String, String> testMap = new HashMap<>();
        testMap.put("time", "20:13:13");
        EnvironmentVariablesNodeProperty.Entry entry = new EnvironmentVariablesNodeProperty.Entry("NODE_TIME",
                testMap.get("time"));
        EnvironmentVariablesNodeProperty envVarsNodeProp = new EnvironmentVariablesNodeProperty(entry);
        j.jenkins.getGlobalNodeProperties().add(envVarsNodeProp);
        GroovyScript script = new GroovyScript(new SecureGroovyScript(SCRIPT, Boolean.FALSE, null),
                new SecureGroovyScript(FALLBACK_SCRIPT, Boolean.FALSE, null));
        ChoiceParameter param = new ChoiceParameter("param000", "description", "randomName", script,
                CascadeChoiceParameter.ELEMENT_TYPE_FORMATTED_HIDDEN_HTML, true, 0);

        assertEquals(Arrays.asList("a", "b", "20:13:13").toString(), param.getChoices().values().toString());
    }

}
