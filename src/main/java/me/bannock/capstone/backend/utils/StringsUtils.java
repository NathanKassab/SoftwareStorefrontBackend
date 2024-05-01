package me.bannock.capstone.backend.utils;

import org.apache.commons.lang3.StringUtils;

public class StringsUtils {

    /**
     * Converts camelCase text into regular reading text. It is used on some thymeleaf pages
     * @param input The input camel case text
     * @return The regular text output
     */
    public static String convertCamelCaseToText(String input){
        return StringUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(input), StringUtils.SPACE));
    }

}
