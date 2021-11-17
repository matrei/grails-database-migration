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
import spock.lang.AutoCleanup

class DbmUpdateSqlCommandSpec extends ApplicationContextDatabaseMigrationCommandSpec {

    @Override
    protected Class<ApplicationCommand> getCommandClass() {
        return DbmUpdateSqlCommand
    }

    @AutoCleanup('delete')
    File outputFile = File.createTempFile('update', 'sql')

    def "writes SQL to update database to STDOUT"() {
        given:
            command.changeLogFile << CHANGE_LOG_CONTENT

        when:
            command.handle(getExecutionContext())

        then:
            def output = output.toString()
            output.contains('CREATE TABLE PUBLIC.author (id BIGINT AUTO_INCREMENT NOT NULL, version BIGINT NOT NULL, name VARCHAR(255) NOT NULL, CONSTRAINT authorPK PRIMARY KEY (id));')
            output.contains('CREATE TABLE PUBLIC.book (id BIGINT AUTO_INCREMENT NOT NULL, version BIGINT NOT NULL, author_id BIGINT NOT NULL, title VARCHAR(255) NOT NULL, CONSTRAINT bookPK PRIMARY KEY (id));')
            output.contains('ALTER TABLE PUBLIC.book ADD CONSTRAINT FK_4sac2ubmnqva85r8bk8fxdvbf FOREIGN KEY (author_id) REFERENCES PUBLIC.author (id);')
            output.contains('INSERT INTO PUBLIC.author (name, version) VALUES (\'Mary\', \'0\');')
            output.contains('INSERT INTO PUBLIC.author (name, version) VALUES (\'Amelia\', \'0\');')
    }

    def "writes SQL to update database with contexts"() {
        given:
            command.changeLogFile << CHANGE_LOG_CONTENT

        when:
            command.handle(getExecutionContext('--contexts=test'))

        then:
            def output = output.toString()
            output.contains('CREATE TABLE PUBLIC.author (id BIGINT AUTO_INCREMENT NOT NULL, version BIGINT NOT NULL, name VARCHAR(255) NOT NULL, CONSTRAINT authorPK PRIMARY KEY (id));')
            output.contains('CREATE TABLE PUBLIC.book (id BIGINT AUTO_INCREMENT NOT NULL, version BIGINT NOT NULL, author_id BIGINT NOT NULL, title VARCHAR(255) NOT NULL, CONSTRAINT bookPK PRIMARY KEY (id));')
            output.contains('ALTER TABLE PUBLIC.book ADD CONSTRAINT FK_4sac2ubmnqva85r8bk8fxdvbf FOREIGN KEY (author_id) REFERENCES PUBLIC.author (id);')
            !output.contains('INSERT INTO PUBLIC.author (name, version) VALUES (\'Mary\', \'0\');')
            output.contains('INSERT INTO PUBLIC.author (name, version) VALUES (\'Amelia\', \'0\');')
    }

    def "writes SQL to update database to a file given as arguments"() {
        given:
            command.changeLogFile << CHANGE_LOG_CONTENT

        when:
            command.handle(getExecutionContext(outputFile.canonicalPath))

        then:
            def output = outputFile.text
            output.contains('CREATE TABLE PUBLIC.author (id BIGINT AUTO_INCREMENT NOT NULL, version BIGINT NOT NULL, name VARCHAR(255) NOT NULL, CONSTRAINT authorPK PRIMARY KEY (id));')
            output.contains('CREATE TABLE PUBLIC.book (id BIGINT AUTO_INCREMENT NOT NULL, version BIGINT NOT NULL, author_id BIGINT NOT NULL, title VARCHAR(255) NOT NULL, CONSTRAINT bookPK PRIMARY KEY (id));')
            output.contains('ALTER TABLE PUBLIC.book ADD CONSTRAINT FK_4sac2ubmnqva85r8bk8fxdvbf FOREIGN KEY (author_id) REFERENCES PUBLIC.author (id);')
            output.contains('INSERT INTO PUBLIC.author (name, version) VALUES (\'Mary\', \'0\');')
            output.contains('INSERT INTO PUBLIC.author (name, version) VALUES (\'Amelia\', \'0\');')
    }

    static final String CHANGE_LOG_CONTENT = '''
databaseChangeLog = {

    changeSet(author: "John Smith", id: "1") {
        createTable(tableName: "author") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "authorPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "John Smith", id: "2") {
        createTable(tableName: "book") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "bookPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "author_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "title", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "John Smith", id: "3") {
        addForeignKeyConstraint(baseColumnNames: "author_id", baseTableName: "book", constraintName: "FK_4sac2ubmnqva85r8bk8fxdvbf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "author")
    }

    changeSet(author: "John Smith", id: "4", context: "development") {
        insert(tableName: "author") {
            column(name: "name", value: "Mary")
            column(name: "version", value: "0")
        }
    }

    changeSet(author: "John Smith", id: "5", context: "test") {
        insert(tableName: "author") {
            column(name: "name", value: "Amelia")
            column(name: "version", value: "0")
        }
    }
}
'''
}
