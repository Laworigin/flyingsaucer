/*
 * {{{ header & license
 * XRAtRuleImpl.java
 * Copyright (c) 2004 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package com.pdoubleya.xhtmlrenderer.css.impl;

import java.util.*;

import org.w3c.dom.css.CSSRule;

import com.pdoubleya.xhtmlrenderer.css.XRStyleSheet;


/**
 * An @ rule in CSS, such as (at)page, (at)import
 *
 * @author    Patrick Wright
 *
 */
public class XRAtRuleImpl extends XRSheetRuleImpl {
    /**
     * seq of 0
     *
     * @param sheet          PARAM
     * @param cssRule        PARAM
     * @param propertyNames  PARAM
     * @param isImportant    PARAM
     */
    public XRAtRuleImpl( XRStyleSheet sheet, CSSRule cssRule, List propertyNames, boolean isImportant ) {
        this( sheet, cssRule, propertyNames, 0, isImportant );
    }


    /**
     * Constructor for the XRAtRuleImpl object
     *
     * @param sheet          PARAM
     * @param cssRule        PARAM
     * @param sequence       PARAM
     * @param propertyNames  PARAM
     * @param isImportant    PARAM
     */
    public XRAtRuleImpl( XRStyleSheet sheet, CSSRule cssRule, List propertyNames, int sequence, boolean isImportant ) {
        super( sheet, cssRule, propertyNames, sequence, isImportant );
    }
}// end interface


