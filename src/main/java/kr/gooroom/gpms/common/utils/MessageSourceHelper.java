/*
 * Copyright 2015-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kr.gooroom.gpms.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.i18n.LocaleContextHolder;

public class MessageSourceHelper {

	private static final Logger logger = LoggerFactory.getLogger(MessageSourceHelper.class);

	private static MessageSource messageSource = null;

	private MessageSourceHelper(MessageSource messageSource) {
		MessageSourceHelper.messageSource = messageSource;
	}

	/**
	 * @return the messageSource
	 */
	public static MessageSource getMessageSource() {
		return messageSource;
	}

	/**
	 * @param messageSource the messageSource to set
	 */
	public static void setMessageSource(MessageSource messageSource) {
		MessageSourceHelper.messageSource = messageSource;
	}

	/**
	 * I18n Support
	 * 
	 * @param code message code
	 * @return message or code if none defined
	 */
	public static String getMessage(String code) {
		try {
			return messageSource == null ? code : messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
		} catch (Exception e) {
			logger.error(e.toString());
			return code;
		}
	}

	/**
	 * I18n Support
	 * 
	 * @param msr message source resolvable
	 * @return message or code if none defined
	 */
	public static String getMessage(MessageSourceResolvable msr) {
		return messageSource == null ? msr.getDefaultMessage()
				: messageSource.getMessage(msr, LocaleContextHolder.getLocale());
	}

	public static String getMessage(String code, Object[] args) {
		return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
	}

	public static String getMessage(String code, String args) {
		return messageSource.getMessage(code, new String[] { args }, LocaleContextHolder.getLocale());
	}
}
