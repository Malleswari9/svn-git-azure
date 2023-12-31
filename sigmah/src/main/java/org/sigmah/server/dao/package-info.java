/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

/**
 * Data Access Objects for {@link org.sigmah.server.domain} classes.
 *
 * There is some {@link http://www.infoq.com/news/2007/09/jpa-dao debate} as to
 * whether a Data Access layer is strictly necessary given the existing
 * level of abstraction of the JPA EntityManager interface.
 *
 * This is probably valid in some cases, but here there are enough complicated queries that its worth
 * centralizing them in one place so that multiple CommandHandlers can share this code.
 *
 * Also, most of the boiler plat 
 */
package org.sigmah.server.dao;