package com.subtitle.parser.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete subtitle file with all blocks.
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubtitleFile {

    private String fileName;
    private String originalContent;
    private List<SubtitleBlock> blocks;

    /**
     * Get the number of subtitle blocks.
     *
     * @return Number of blocks
     */
    public int getBlockCount() {
        return blocks != null ? blocks.size() : 0;
    }

    /**
     * Add a subtitle block.
     *
     * @param block Subtitle block to add
     */
    public void addBlock(SubtitleBlock block) {
        if (blocks == null) {
            blocks = new ArrayList<>();
        }
        blocks.add(block);
    }

    /**
     * Get all blocks with Chinese text.
     *
     * @return List of blocks containing Chinese
     */
    public List<SubtitleBlock> getChineseBlocks() {
        if (blocks == null) return List.of();
        return blocks.stream()
                .filter(SubtitleBlock::containsChinese)
                .toList();
    }

    /**
     * Build the complete SRT content.
     *
     * @return Formatted SRT content
     */
    public String buildSrtContent() {
        if (blocks == null || blocks.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (SubtitleBlock block : blocks) {
            sb.append(block.toString());
        }
        return sb.toString();
    }
}
