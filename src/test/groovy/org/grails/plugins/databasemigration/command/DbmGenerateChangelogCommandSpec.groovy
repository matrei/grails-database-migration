/*
 * Copyright 2015 original authors
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
package org.grails.plugins.databasemigration.command

import grails.dev.commands.ApplicationCommand

class DbmGenerateChangelogCommandSpec extends ApplicationContextDatabaseMigrationCommandSpec {

    @Override
    protected Class<ApplicationCommand> getCommandClass() {
        return DbmGenerateChangelogCommand
    }

    def setup() {
        sql.executeUpdate '''
CREATE TABLE book (id INT AUTO_INCREMENT NOT NULL, title VARCHAR(255) NOT NULL, price INT NOT NULL, CONSTRAINT PK_BOOK PRIMARY KEY (id));
CREATE TABLE author (id INT AUTO_INCREMENT NOT NULL, name VARCHAR(255) NOT NULL, CONSTRAINT PK_AUTHOR PRIMARY KEY (id));
'''
    }

    def "generates an initial changelog from the database to STDOUT"() {
        when:
            command.handle(getExecutionContext())

        then:
            extractOutput(output) =~ '''
databaseChangeLog = \\{

    changeSet\\(author: ".*?", id: ".*?"\\) \\{
        createTable\\(tableName: "AUTHOR"\\) \\{
            column\\(autoIncrement: "true", name: "ID", type: "INT"\\) \\{
                constraints\\(nullable: "false", primaryKey: "true", primaryKeyName: "PK_AUTHOR"\\)
            \\}

            column\\(name: "NAME", type: "VARCHAR\\(255\\)"\\) \\{
                constraints\\(nullable: "false"\\)
            \\}
        \\}
    \\}

    changeSet\\(author: ".*?", id: ".*?"\\) \\{
        createTable\\(tableName: "BOOK"\\) \\{
            column\\(autoIncrement: "true", name: "ID", type: "INT"\\) \\{
                constraints\\(nullable: "false", primaryKey: "true", primaryKeyName: "PK_BOOK"\\)
            \\}

            column\\(name: "TITLE", type: "VARCHAR\\(255\\)"\\) \\{
                constraints\\(nullable: "false"\\)
            \\}

            column\\(name: "PRICE", type: "INT"\\) \\{
                constraints\\(nullable: "false"\\)
            \\}
        \\}
    \\}
\\}
'''.replaceAll(/\s/, "")
    }

    def "generates an initial changelog from the database to a file given as arguments"() {
        given:
            def outputChangeLog = new File(changeLogLocation, 'changelog.groovy')

        when:
            command.handle(getExecutionContext(outputChangeLog.name))

        then:
            outputChangeLog.text?.replaceAll(/\s/, "") =~ '''
databaseChangeLog = \\{

    changeSet\\(author: ".*?", id: ".*?"\\) \\{
        createTable\\(tableName: "AUTHOR"\\) \\{
            column\\(autoIncrement: "true", name: "ID", type: "INT"\\) \\{
                constraints\\(nullable: "false", primaryKey: "true", primaryKeyName: "PK_AUTHOR"\\)
            \\}

            column\\(name: "NAME", type: "VARCHAR\\(255\\)"\\) \\{
                constraints\\(nullable: "false"\\)
            \\}
        \\}
    \\}

    changeSet\\(author: ".*?", id: ".*?"\\) \\{
        createTable\\(tableName: "BOOK"\\) \\{
            column\\(autoIncrement: "true", name: "ID", type: "INT"\\) \\{
                constraints\\(nullable: "false", primaryKey: "true", primaryKeyName: "PK_BOOK"\\)
            \\}

            column\\(name: "TITLE", type: "VARCHAR\\(255\\)"\\) \\{
                constraints\\(nullable: "false"\\)
            \\}

            column\\(name: "PRICE", type: "INT"\\) \\{
                constraints\\(nullable: "false"\\)
            \\}
        \\}
    \\}
\\}
'''.replaceAll(/\s/, "")
    }
}
