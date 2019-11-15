package org.jqassistant.contrib.plugin.asciidocreport.include;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.io.FilenameUtils;
import org.jqassistant.contrib.plugin.asciidocreport.RuleResult;

/**
 * A filter for rules.
 *
 * TODO Move this to jQA core report.
 *
 * @param <T>
 *            The value type.
 */
public class RuleFilter<T> {

    /**
     * Match {@link RuleResult}s by the given filter.
     *
     * Accepts a comma separated list of filter patterns, each one may contain
     * wildcards "*" or "?". Any pattern starting with "!" will be interpreted as
     * exlcusion.
     *
     * @param rules
     *            The rules ids to match.
     * @param filter
     *            The filter.
     * @return The matching {@link RuleResult}s.
     */
    public Set<String> match(Iterable<String> rules, String filter) {
        Set<String> matches = new HashSet<>();
        if (filter != null) {
            List<String> rulePatterns = asList(filter.split("\\s*,\\s*"));

            List<String> includePatterns = rulePatterns.stream().filter(pattern -> !pattern.startsWith("!")).collect(toList());
            List<String> excludePatterns = rulePatterns.stream().filter(pattern -> pattern.startsWith("!")).map(pattern -> pattern.substring(1))
                    .collect(toList());

            apply(rules, includePatterns, rule -> matches.add(rule));
            apply(rules, excludePatterns, rule -> matches.remove(rule));
        }
        return matches;
    }

    private void apply(Iterable<String> rules, List<String> patterns, Consumer<String> consumer) {
        for (String rule : rules) {
            for (String pattern : patterns) {
                if (FilenameUtils.wildcardMatch(rule, pattern)) {
                    consumer.accept(rule);
                }
            }
        }
    }

}
