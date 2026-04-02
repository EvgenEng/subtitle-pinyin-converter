package com.subtitle.parser.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single subtitle block in SRT format.
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubtitleBlock {

    private int index;
    private String startTime;
    private String endTime;
    private String originalText;
    private String convertedText;

    /**
     * Creates a copy of this block.
     *
     * @return Copy of the subtitle block
     */
    public SubtitleBlock copy() {
        return SubtitleBlock.builder()
                .index(this.index)
                .startTime(this.startTime)
                .endTime(this.endTime)
                .originalText(this.originalText)
                .convertedText(this.convertedText)
                .build();
    }

    /**
     * Checks if the block contains Chinese characters.
     *
     * @return true if contains Chinese, false otherwise
     */
    public boolean containsChinese() {
        if (originalText == null) return false;
        return originalText.codePoints()
                .anyMatch(codepoint ->
                        Character.UnicodeScript.of(codepoint) == Character.UnicodeScript.HAN);
    }

    /**
     * Returns formatted SRT block string.
     *
     * @return Formatted SRT block
     */
    @Override
    public String toString() {
        return String.format("%d%n%s --> %s%n%s%n",
                index, startTime, endTime,
                convertedText != null ? convertedText : originalText);
    }
}
