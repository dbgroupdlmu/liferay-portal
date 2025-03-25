/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.dao.jdbc.kingbase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.kingbase8.KBConnection;
import com.kingbase8.KBStatement;
import com.kingbase8.largeobject.LargeObject;
import com.kingbase8.largeobject.LargeObjectManager;

/**
 * @author Bai Mei
 */
public class KingbaseJDBCUtil {

	public static byte[] getLargeObject(ResultSet resultSet, String name)
		throws SQLException {

		long id = resultSet.getLong(name);

		if (id == 0) {
			return null;
		}

		Statement statement = resultSet.getStatement();

		Connection connection = statement.getConnection();

		boolean autoCommit = connection.getAutoCommit();

		if (autoCommit) {
			connection.setAutoCommit(false);
		}

		try {
			KBConnection kbConnection = connection.unwrap(KBConnection.class);

			LargeObjectManager largeObjectManager =
				kbConnection.getLargeObjectAPI();

			LargeObject largeObject = largeObjectManager.open(
				id, LargeObjectManager.READ);

			byte[] bytes = new byte[largeObject.size()];

			largeObject.read(bytes, 0, largeObject.size());

			largeObject.close();

			return bytes;
		}
		finally {
			if (autoCommit) {
				connection.setAutoCommit(true);
			}
		}
	}

	public static boolean isKBStatement(Statement statement)
		throws SQLException {

		if (statement.isWrapperFor(KBStatement.class)) {
			return true;
		}

		return false;
	}

	public static void setLargeObject(
			PreparedStatement preparedStatement, int index, byte[] bytes)
		throws SQLException {

		Connection connection = preparedStatement.getConnection();

		boolean autoCommit = connection.getAutoCommit();

		if (autoCommit) {
			connection.setAutoCommit(false);
		}

		try {
			KBConnection kbConnection = connection.unwrap(KBConnection.class);

			LargeObjectManager largeObjectManager =
				kbConnection.getLargeObjectAPI();

			long id = largeObjectManager.createLO(
				LargeObjectManager.READ | LargeObjectManager.WRITE);

			LargeObject largeObject = largeObjectManager.open(
				id, LargeObjectManager.WRITE);

			largeObject.write(bytes);

			largeObject.close();

			preparedStatement.setLong(index, id);
		}
		finally {
			if (autoCommit) {
				connection.setAutoCommit(true);
			}
		}
	}

}