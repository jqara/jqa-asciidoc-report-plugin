package org.jqassistant.contrib.plugin.asciidocreport.include;

import static java.util.stream.Collectors.toList;

import java.util.*;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.asciidoctor.ast.AbstractBlock;
import org.jqassistant.contrib.plugin.asciidocreport.RuleResult;

@Slf4j
public class SummaryFilter {

    @Builder
    @Getter
    @ToString
    static class Result {

        private Collection<RuleResult> constraints;

        private Collection<RuleResult> concepts;

    }

    private final Map<String, RuleResult> conceptResults;
    private final Map<String, RuleResult> constraintResults;
    private final Map<String, AbstractBlock> ruleBlocks;

    private final RuleFilter<RuleResult> ruleFilter;

    public SummaryFilter(Map<String, RuleResult> conceptResults, Map<String, RuleResult> constraintResults, Map<String, AbstractBlock> ruleBlocks,
            RuleFilter<RuleResult> ruleFilter) {
        this.conceptResults = conceptResults;
        this.constraintResults = constraintResults;
        this.ruleBlocks = ruleBlocks;
        this.ruleFilter = ruleFilter;
    }

    public Result apply(Map<String, Object> attributes) {
        if (attributes.isEmpty()) {
            // No filters provided, render everything
            return Result.builder().constraints(constraintResults.values()).concepts(conceptResults.values()).build();
        } else {
            // Apply filters, render only selected rules
            List<RuleResult> constraints = include(attributes, "constraints", "importedConstraints", constraintResults);
            List<RuleResult> concepts = include(attributes, "concepts", "importedConcepts", conceptResults);
            if (concepts.isEmpty() && constraints.isEmpty()) {
                log.warn("No constraints/concepts found matching the given filters {}.", attributes);
            }
            return Result.builder().constraints(constraints).concepts(concepts).build();
        }
    }

    private List<RuleResult> include(Map<String, Object> attributes, String rulesAttribute, String importedRulesAttribute, Map<String, RuleResult> results) {
        String rulesFilter = (String) attributes.get(rulesAttribute);
        String importedRulesFilter = (String) attributes.get(importedRulesAttribute);
        return filterRuleResults(results, rulesFilter, importedRulesFilter);
    }

    private List<RuleResult> filterRuleResults(Map<String, RuleResult> results, String rulesFilter, String importedRulesFilter) {
        Set<String> ruleResults = new HashSet<>();
        // collect rules from documents
        Set<String> rules = results.keySet().stream().filter(rule -> ruleBlocks.keySet().contains(rule)).collect(Collectors.toSet());
        ruleResults.addAll(ruleFilter.match(rules, rulesFilter));
        // collect imported rules
        Set<String> importedRules = results.keySet().stream().filter(rule -> !ruleBlocks.keySet().contains(rule)).collect(Collectors.toSet());
        ruleResults.addAll(ruleFilter.match(importedRules, importedRulesFilter));
        return results.entrySet().stream().filter(entry -> ruleResults.contains(entry.getKey())).map(entry -> entry.getValue()).collect(toList());
    }

}