package com.webapp.bankingplatform.ach;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses a NACHA file (94-char fixed-width lines) into a list of records.
 * Used for inbound files and acknowledgements.
 */
@Component
public class NachaParser {

    public record ParsedRecord(char type, String raw) {}

    public List<ParsedRecord> parse(String content) {
        List<ParsedRecord> out = new ArrayList<>();
        if (content == null || content.isBlank()) return out;
        for (String line : content.split("\\r?\\n")) {
            if (line.isEmpty()) continue;
            if (line.length() != NachaConstants.RECORD_LENGTH) {
                throw new IllegalArgumentException(
                    "Invalid record length " + line.length() + " (expected 94): " + line);
            }
            char type = line.charAt(0);
            if (type == '9') continue; // padding
            out.add(new ParsedRecord(type, line));
        }
        return out;
    }

    public String routingFromImmediateDestination(String immDest) {
        // " 121000358" -> "121000358"
        return immDest.substring(1, 10);
    }

    public String fileIdFromHeader(String headerLine) {
        return headerLine.substring(33, 34);
    }
}
