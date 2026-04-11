package org.project.pet_health.utils;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class SensitiveWordFilter {

    // 构建的 DFA 敏感词树
    private Map<Object, Object> sensitiveWordMap = null;

    /**
     * 初始化敏感词树（将数据库里的词加载到内存）
     * @param sensitiveWords 敏感词集合
     */
    public void initSensitiveWordMap(Set<String> sensitiveWords) {
        sensitiveWordMap = new HashMap<>(sensitiveWords.size());
        for (String word : sensitiveWords) {
            Map<Object, Object> currentMap = sensitiveWordMap;
            for (int i = 0; i < word.length(); i++) {
                char keyChar = word.charAt(i);
                // 获取当前的子节点
                Object wordMap = currentMap.get(keyChar);

                if (wordMap != null) {
                    // 如果节点存在，直接进入下一层
                    currentMap = (Map<Object, Object>) wordMap;
                } else {
                    // 如果节点不存在，新建节点并放入 currentMap 中
                    Map<Object, Object> newMap = new HashMap<>();
                    newMap.put("isEnd", "0"); // "0"表示还不是词的结尾
                    currentMap.put(keyChar, newMap);
                    currentMap = newMap;
                }

                // 如果当前字符是该词的最后一个字符，将结束标志设为"1"
                if (i == word.length() - 1) {
                    currentMap.put("isEnd", "1");
                }
            }
        }
    }

    /**
     * 核心过滤方法：替换文本中的敏感词
     * @param text 用户输入的文本 (例如社区帖子内容)
     * @return 过滤后的文本
     */
    public String replaceSensitiveWord(String text) {
        if (sensitiveWordMap == null || sensitiveWordMap.isEmpty()) {
            return text;
        }
        StringBuilder result = new StringBuilder(text);
        int i = 0;
        while (i < text.length()) {
            int matchLength = checkSensitiveWord(text, i);
            if (matchLength > 0) {
                // 找到了敏感词，进行替换为 *
                for (int j = 0; j < matchLength; j++) {
                    result.setCharAt(i + j, '*');
                }
                i = i + matchLength; // 跳过已替换的长度
            } else {
                i++;
            }
        }
        return result.toString();
    }

    /**
     * 检查从指定位置开始，是否匹配到敏感词
     * @return 匹配到的敏感词长度，如果没有匹配到返回0
     */
    private int checkSensitiveWord(String text, int beginIndex) {
        boolean matchFlag = false;
        int matchLength = 0;
        Map<Object, Object> currentMap = sensitiveWordMap;

        for (int i = beginIndex; i < text.length(); i++) {
            char word = text.charAt(i);
            currentMap = (Map<Object, Object>) currentMap.get(word);
            if (currentMap != null) {
                // 找到了字符，匹配长度+1
                matchLength++;
                // 如果当前节点是结束节点，说明找到了一个完整的敏感词
                if ("1".equals(currentMap.get("isEnd"))) {
                    matchFlag = true;
                }
            } else {
                break; // 树中没有这个字符，匹配中断
            }
        }
        if (!matchFlag) {
            matchLength = 0;
        }
        return matchLength;
    }
}
