package com.yc.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@Component
public class SensitiveFilter {
    private TrieNode rootNode;

    private static final Logger logger =  LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT = "***";

    @PostConstruct//be executed after dependency injection is done to perform any initialization.
    public void init() {
        rootNode = new TrieNode();
        try (
            InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream));
        ) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                this.addKeyword(line);
            }
        } catch (Exception e) {
            logger.error("加载敏感词失败:" + e.getMessage());
        }
    }

    public void addKeyword(String keyword) {
        TrieNode tempNode = this.rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
//            if (subNode != null) {
//                tempNode = subNode;
//            } else {
//                subNode = new TrieNode();
//                tempNode.addSubNode(c, subNode);
//                tempNode = subNode;
//            }
            if (subNode == null) {
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }

            tempNode = subNode;

            if (i == (keyword.length() - 1)) {
                subNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
//    public String doFilter(String text) {
//        if (StringUtils.isBlank(text)) {
//            return null;
//        }
//
//        StringBuilder textBuilder = new StringBuilder(text);
//        int index1 = 0, index2 = index1;
//        do {
//            TrieNode tempNode = rootNode;
//            index2 = index1;
//            do {
//                TrieNode subNode;
//                if ((subNode = tempNode.getSubNode(textBuilder.charAt(index2))) != null) {
//                    if (subNode.isKeywordEnd) {
//                        textBuilder.replace(index1, index2 + 1, REPLACEMENT);
//                        index1 = index2+1;
//                        break;
//                    }
//                    if (index2 == textBuilder.length() - 1) {
//                        index1 += 1;
//                        break;
//                    }
//                    do {
//                        index2 += 1;
//                        if (index2 == textBuilder.length()) {
//                            index1 +=1;
//                            break;
//                        }
//                    } while (isSymbol(textBuilder.charAt(index2)));
//                    tempNode = subNode;
//                } else {
//                    index1 += 1;
//                    break;
//                }
//            } while (index2 < textBuilder.length());
//        }while (index1 < textBuilder.length());
//        return textBuilder.toString();
//    }
    public String doFilter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        // 指针1
        TrieNode tempNode = rootNode;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;
        // 结果
        StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);

            // 跳过符号
            if (isSymbol(c)) {
                // 若指针1处于根节点,将此符号计入结果,让指针2向下走一步
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                // 无论符号在开头或中间,指针3都向下走一步
                position++;
                continue;
            }

            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                // 以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                // 进入下一个位置
                position = ++begin;
                // 重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                // 发现敏感词,将begin~position字符串替换掉
                sb.append(REPLACEMENT);
                // 进入下一个位置
                begin = ++position;
                // 重新指向根节点
                tempNode = rootNode;
            } else {
                // 检查下一个字符
                position++;
            }
        }

        // 将最后一批字符计入结果
        sb.append(text.substring(begin));

        return sb.toString();
    }

    //判断是否为符号
    private boolean isSymbol(Character c) {
        return (!CharUtils.isAsciiAlphanumeric(c)) && (c < 0x2E80 || c > 0x9FFF);
    }

    //前缀树
    private class TrieNode {
        private Map<Character,TrieNode> map = new HashMap<>();
        private boolean isKeywordEnd;

        public void addSubNode(Character character, TrieNode trieNode) {
            map.put(character, trieNode);
        }

        public TrieNode getSubNode(Character character) {
            return map.get(character);
        }

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }
    }

    /**
     * 遍历前缀树（广度优先）
     * 提供给测试使用。测试时调用公有方法，而不需要通过反射访问私有变量
     */
    public void method() {
        Queue<TrieNode> queue = new ArrayDeque<>();
        queue.add(rootNode);
        while (!queue.isEmpty()) {
            TrieNode poll = queue.poll();
            if (!poll.isKeywordEnd) {
                Set<Character> characters = poll.map.keySet();
                for (Character character : characters) {
                    System.out.print(character);
                    queue.add(poll.getSubNode(character));
                }
            }
        }
    }
}
