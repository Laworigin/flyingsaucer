/*
 * {{{ header & license
 * XRPropertyImpl.java
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

import com.pdoubleya.xhtmlrenderer.css.constants.ValueConstants;
import java.util.*;
import java.util.logging.*;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSValue;

import com.pdoubleya.xhtmlrenderer.css.XRElement;
import com.pdoubleya.xhtmlrenderer.css.XRProperty;
import com.pdoubleya.xhtmlrenderer.css.XRValue;
import com.pdoubleya.xhtmlrenderer.css.constants.CSSName;
import com.pdoubleya.xhtmlrenderer.css.factory.BorderColorPropertyFactory;
import com.pdoubleya.xhtmlrenderer.css.factory.BorderPropertyFactory;
import com.pdoubleya.xhtmlrenderer.css.factory.BorderSidePropertyFactory;
import com.pdoubleya.xhtmlrenderer.css.factory.BorderStylePropertyFactory;
import com.pdoubleya.xhtmlrenderer.css.factory.BorderWidthPropertyFactory;
import com.pdoubleya.xhtmlrenderer.css.factory.MarginPropertyFactory;
import com.pdoubleya.xhtmlrenderer.css.factory.PaddingPropertyFactory;
import com.pdoubleya.xhtmlrenderer.css.factory.PropertyFactory;
import org.joshy.html.Context;

import org.joshy.html.css.RuleNormalizer;


/**
 * Default XRProperty implementation.
 *
 * @author    Patrick Wright
 *
 */
public class XRPropertyImpl implements XRProperty {
    /**
     * The property name to PropertyFactory mappings. The factories facilitate
     * creating XRProperties from a property definition. Note static block is at
     * end of class.
     */
    private final static Map PRP_FACTORIES;

    /** Static RuleNormalizer for cleaning property values. */
    private final static RuleNormalizer RULE_NORMALIZER;

    /** The Style where this property originated. */
    private CSSStyleDeclaration _style;

    /** Property's text name, e.g. "margin-top" */
    private String _propName;

    /** Sequence in which this property was found in declaration, 0-based. */
    private int _sequence;

    /** The value as specified in the declaration */
    private XRValue _specifiedValue;

    /** Relative value as computed for the current context */
    private XRValue _computedValue;

    /** Computed value as restricted for the current environment */
    private XRValue _actualValue;

    /**
     * Whether the property has been resolved relative to the current
     * environment or not
     */
    private boolean _isResolved;

    // seq = 0;
    /**
     * Constructor for the XRPropertyImpl object
     *
     * @param style     PARAM
     * @param propName  PARAM
     */
    public XRPropertyImpl( CSSStyleDeclaration style, String propName ) {
        this( style, propName, 0 );
    }


    /**
     * Constructor for the XRPropertyImpl object
     *
     * @param style     PARAM
     * @param propName  PARAM
     * @param sequence  PARAM
     */
    public XRPropertyImpl( CSSStyleDeclaration style, String propName, int sequence ) {
        this( style, propName, sequence, new XRValueImpl( (CSSPrimitiveValue)style.getPropertyCSSValue( propName ),
                style.getPropertyPriority( propName ) ) );
    }


    /**
     * Constructor for the XRPropertyImpl object
     *
     * @param style     PARAM
     * @param propName  PARAM
     * @param sequence  PARAM
     * @param value     PARAM
     */
    public XRPropertyImpl( CSSStyleDeclaration style, String propName, int sequence, XRValue value ) {
        _style = style;
        _propName = propName;
        _sequence = sequence;
        _specifiedValue = value;
    }

    /**
     * Utility method to instantiate a group of XRProperties out of a style
     * rule/declaration. Returns an iterator of the instantiated properties--the
     * are not attached to the declaration or otherwise stored.
     *
     * @param style     PARAM
     * @param propName  PARAM
     * @param sequence  PARAM
     * @param cssRule   PARAM
     * @return          Returns
     */
    public static Iterator fromCSSPropertyDecl( CSSRule cssRule, CSSStyleDeclaration style, String propName, int sequence ) {
        // HACK: special cases for RuleNormalizer...need to work this out cleanly
        PropertyFactory factory = (PropertyFactory)PRP_FACTORIES.get( propName );
        if ( ( factory == null && cssRule.getType() == CSSRule.STYLE_RULE ) ||
                propName.equals( CSSName.BORDER_COLOR_SHORTHAND ) ) {
            RULE_NORMALIZER.normalize( (CSSStyleRule)cssRule );
            style = ( (CSSStyleRule)cssRule ).getStyle();
        }

        List list = new ArrayList();
        switch ( style.getPropertyCSSValue( propName ).getCssValueType() ) {
            case CSSValue.CSS_PRIMITIVE_VALUE:
                if ( factory == null ) {
                    XRProperty prop = new XRPropertyImpl( style, propName, sequence );
                    list.add( prop );
                } else {
                    Iterator iter = factory.explodeProperties( style, propName, sequence );
                    while ( iter.hasNext() ) {
                        list.add( iter.next() );
                    }
                }
                break;
            case CSSValue.CSS_VALUE_LIST:
                if ( factory == null ) {
                    XRProperty prop = new XRPropertyImpl( style, propName, sequence );
                    list.add( prop );

                    // HACK--right now, BP is allowed to not have a factory
                    if ( !propName.equals( CSSName.BACKGROUND_POSITION ) && !propName.equals( CSSName.FONT_FAMILY ) ) {
                        System.err.println( "! Property '" + propName + "' was assigned multiple values but no factory is assigned to it." );
                    }
                } else {
                    Iterator iter = factory.explodeProperties( style, propName, sequence );
                    while ( iter.hasNext() ) {
                        list.add( iter.next() );
                    }
                }
                break;
            default:
                System.err.println( "Property '" + propName + "' is neither primitive nor value list, can't handle." );
                break;
        }

        return list.iterator();
    }

    /**
     * Deep copy operation. However, any contained SAC instances are not
     * deep-copied.
     *
     * @return   Returns
     */
    public XRProperty copyOf() {
        return new XRPropertyImpl( _style, _propName, _sequence, _specifiedValue.copyOf() );
    }


    /**
     * The computed value, with any modifications forced by the presentation
     * environment e.g. limitation in color range.
     *
     * @return   Returns
     */
    public XRValue actualValue() {
        return ( ValueConstants.isAbsoluteUnit(_specifiedValue.cssValue()) ? _specifiedValue : _actualValue );
    }


    /**
     * The value as specified in the sheet.
     *
     * @return   Returns
     */
    public XRValue specifiedValue() {
        return _specifiedValue;
    }


    /**
     * The plain-text property name, should be CSS2 valid.
     *
     * @return   Returns
     */
    public String propertyName() {
        return _propName;
    }


    /**
     * ...
     *
     * @return   Returns
     */
    public String toString() {
        return _propName + "=" + _specifiedValue;
    }


    /**
     * If the property has a relative value that has not been computed, computes
     * it. After the property value is resolved, computedValue() and
     * actualValue() will return meaningful results.
     *
     * @param elemContext  PARAM
     * @param context      PARAM
     */
    public synchronized void resolveValue( Context context, XRElement elemContext ) {
        if ( isResolved() ) {
            return;
        } else {
            if ( isResolvable() ) {
                XRValue computed = _specifiedValue;

                // inherit the value from parent element if value is set to inherit
                if ( _specifiedValue.forcedInherit() ) {
                    XRElement parent = elemContext.parentXRElement();
                    if ( parent == null ) {
                        System.err.println( "XRPropertyImpl: trying to resolve an inherited property, but have no parent XRElement (root of document?)--property '" + propertyName() + "' may not be defined in CSS." );
                        // ASK: probably a good place to shove initial values?
                    } else {
                        // this is indirectly recursive because propertyByName() will end up here again
                        // but in parent element context
                        computed = parent.derivedStyle().propertyByName( context, propertyName() ).actualValue();
                    }
                }

                // if value is relative value (e.g. percentage), resolve it
                if ( !ValueConstants.isAbsoluteUnit(computed.cssValue()) && !computed.requiresComputation() ) {
                    computed.computeRelativeUnit( context, elemContext, propertyName() );
                }

                _computedValue = computed;
                _actualValue = computed;
            } else {
                _computedValue = _specifiedValue;
                _actualValue = _specifiedValue;
            }
            _isResolved = true;
        }
    }


    /**
     * Returns true if the property has an absolute value, or a relative value
     * that has been correctly computed.
     *
     * @return   The resolved value
     */
    public boolean isResolved() {
        return ValueConstants.isAbsoluteUnit(_specifiedValue.cssValue()) || _isResolved;
    }


    /**
     * Gets the resolvable attribute of the XRPropertyImpl object
     *
     * @return   The resolvable value
     */
    public boolean isResolvable() {
        return _specifiedValue.isPrimitiveType() && !ValueConstants.isAbsoluteUnit(_specifiedValue.cssValue());
    }

    static {
        RULE_NORMALIZER = new RuleNormalizer();

        PRP_FACTORIES = new HashMap();

        PRP_FACTORIES.put( CSSName.MARGIN_SHORTHAND, MarginPropertyFactory.instance() );

        PRP_FACTORIES.put( CSSName.PADDING_SHORTHAND, PaddingPropertyFactory.instance() );

        PRP_FACTORIES.put( CSSName.BORDER_SHORTHAND, BorderPropertyFactory.instance() );

        PRP_FACTORIES.put( CSSName.BORDER_WIDTH_SHORTHAND, BorderWidthPropertyFactory.instance() );
        PRP_FACTORIES.put( CSSName.BORDER_COLOR_SHORTHAND, BorderColorPropertyFactory.instance() );
        PRP_FACTORIES.put( CSSName.BORDER_STYLE_SHORTHAND, BorderStylePropertyFactory.instance() );

        PRP_FACTORIES.put( CSSName.BORDER_TOP_SHORTHAND, BorderSidePropertyFactory.instance() );
        PRP_FACTORIES.put( CSSName.BORDER_RIGHT_SHORTHAND, BorderSidePropertyFactory.instance() );
        PRP_FACTORIES.put( CSSName.BORDER_BOTTOM_SHORTHAND, BorderSidePropertyFactory.instance() );
        PRP_FACTORIES.put( CSSName.BORDER_LEFT_SHORTHAND, BorderSidePropertyFactory.instance() );
    }
}// end class

