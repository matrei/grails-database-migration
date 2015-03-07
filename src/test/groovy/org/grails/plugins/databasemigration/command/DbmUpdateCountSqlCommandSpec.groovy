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
import org.grails.plugins.databasemigration.DatabaseMigrationException
import spock.lang.AutoCleanup

class DbmUpdateCountSqlCommandSpec extends ApplicationContextDatabaseMigrationCommandSpec {

    final Class<ApplicationCommand> commandClass = DbmUpdateCountSqlCommand

    @AutoCleanup('delete')
    File outputFile = File.createTempFile('update', 'sql')

    def "writes SQL to apply next NUM changes to the database to STDOUT"() {
        given:
            command.changeLogFile << CHANGE_LOG_CONTENT

        when:
            command.handle(getExecutionContext('1'))

        then:
            def output = outputCapture.toString()
            output.contains('CREATE TABLE PUBLIC.author (id BIGINT AUTO_INCREMENT NOT NULL, version BIGINT NOT NULL, name VARCHAR(255) NOT NULL, CONSTRAINT authorPK PRIMARY KEY (id));')
            !output.contains('CREATE TABLE PUBLIC.book (id BIGINT AUTO_INCREMENT NOT NULL, version BIGINT NOT NULL, author_id BIGINT NOT NULL, title VARCHAR(255) NOT NULL, CONSTRAINT bookPK PRIMARY KEY (id));')
    }

    def "writes SQL to apply next NUM changes to the database to a file given as arguments"() {
        given:
            command.changeLogFile << CHANGE_LOG_CONTENT

        when:
            command.handle(getExecutionContext('1', outputFile.canonicalPath))

        then:
            def output = outputFile.text
            output.contains('CREATE TABLE PUBLIC.author (id BIGINT AUTO_INCREMENT NOT NULL, version BIGINT NOT NULL, name VARCHAR(255) NOT NULL, CONSTRAINT authorPK PRIMARY KEY (id));')
            !output.contains('CREATE TABLE PUBLIC.book (id BIGINT AUTO_INCREMENT NOT NULL, version BIGINT NOT NULL, author_id BIGINT NOT NULL, title VARCHAR(255) NOT NULL, CONSTRAINT bookPK PRIMARY KEY (id));')
    }

    def "an error occurs if the count parameter is not specified"() {
        when:
            command.handle(getExecutionContext())

        then:
            def e = thrown(DatabaseMigrationException)
            e.message == "The ${command.name} command requires a change set number argument"
    }

    def "an error occurs if the count parameter is not number"() {
        when:
            command.handle(getExecutionContext('one'))

        then:
            def e = thrown(DatabaseMigrationException)
            e.message == 'The change set number argument \'one\' isn\'t a number'
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
