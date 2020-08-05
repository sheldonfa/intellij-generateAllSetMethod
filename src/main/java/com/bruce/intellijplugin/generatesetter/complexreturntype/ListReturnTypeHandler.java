/*
 *  Copyright (c) 2017-2019, bruce.ge.
 *    This program is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU General Public License
 *    as published by the Free Software Foundation; version 2 of
 *    the License.
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *    GNU General Public License for more details.
 *    You should have received a copy of the GNU General Public License
 *    along with this program;
 */

package com.bruce.intellijplugin.generatesetter.complexreturntype;

import com.bruce.intellijplugin.generatesetter.Parameters;
import com.bruce.intellijplugin.generatesetter.utils.PsiToolUtils;
import com.google.common.collect.Sets;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.impl.source.PsiClassImpl;
import org.jetbrains.annotations.NotNull;

/**
 * @Author bruce.ge
 * @Date 2017/1/28
 * @Description
 */
public class ListReturnTypeHandler implements ComplexReturnTypeHandler {

    @NotNull
    @Override
    public InsertDto handle(Parameters returnParamInfo, PsiClassImpl psiClass , String splitText, PsiParameter[] parameters, boolean hasGuava) {
        InsertDto insertDto = new InsertDto();
        insertDto.setImportList(Sets.newHashSet("import java.util.stream.Collectors;"));
        String returnVariableName = "";
        StringBuilder insertText = new StringBuilder();
        insertText.append(splitText);
        ListParamInfo paramInfo = null;
        for (PsiParameter parameter : parameters) {
//            todo for array class how to fix it.
            //the list object shall have set method so it can work.
            Parameters wrapInfo = PsiToolUtils.extractParamInfo(parameter.getType());
            if (wrapInfo.getCollectPackege() == null) {
                continue;
            } else {
                //check the collectType.
                String qualifyTypeName = wrapInfo.getCollectPackege();
                if (qualifyTypeName.equals("java.util.List")
                        || qualifyTypeName.equals("java.util.Set")) {
                    //there must be one param for it.
                    paramInfo = new ListParamInfo();
                    paramInfo.setCollectType(qualifyTypeName);
                    paramInfo.setParamName(parameter.getName());
                    paramInfo.setRealType(wrapInfo.getParams().get(0).getRealName());
                } else {
                    continue;
                }
            }
        }
        if (paramInfo != null) {
            String realType = paramInfo.getRealType();
            String varName = PsiToolUtils.lowerStart(paramInfo.getRealType());
            String methodName = "to";
            if (returnParamInfo.getParams().size() > 0) {
                methodName = methodName + returnParamInfo.getParams().get(0).getRealName();
            }
            // 修改为 stream方式
//            insertText.append(splitText + "for (" + realType + " " + varName + " :" + paramInfo.getParamName() + ") {");
//            insertText.append(splitText + "\t" + returnVariableName + ".add(" + methodName + "(" + varName + "));");
//            insertText.append(splitText + "}");
            insertText.append("return "+paramInfo.getParamName()+".stream().map("+psiClass.getName()+"::"+methodName+").collect(Collectors.toList());");
        }
        insertDto.setAddedText(insertText.toString());
        return insertDto;
    }

    private static String generateAddTextForCollectParam(NewMethodInfo deepInfo, Parameters returnParamInfo, String returnVariableName, String splitText) {
        String methodName = "convertTo";
        if (returnParamInfo.getParams().size() > 0) {
            methodName = methodName + returnParamInfo.getParams().get(0).getRealName();
        }
        StringBuilder insertText = new StringBuilder();
        insertText.append(splitText + "for(int i=0;i<" + deepInfo.getParamName() + ".size();i++){");
        insertText.append(splitText + "\t" + returnVariableName + ".add(" + methodName + "(" + deepInfo.getParamName() + ".get(i)));");
        insertText.append(splitText + "}");
        return insertText.toString();
    }
}
