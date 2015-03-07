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

class DbmRollbackSqlCommandSpec extends ApplicationContextDatabaseMigrationCommandSpec {

    final Class<ApplicationCommand> commandClass = DbmRollbackSqlCommand

    @AutoCleanup('delete')
    File outputFile = File.createTempFile('rollback', 'sql')

    def setup() {
        command.changeLogFile << CHANGE_LOG_CONTENT

        new DbmUpdateCountCommand(applicationContext: applicationContext).handle(getExecutionContext('1'))
        new DbmTagCommand().handle(getScriptExecutionContext('test-tag'))
        new DbmUpdateCommand(applicationContext: applicationContext).handle(getExecutionContext())

        def tables = sql.rows('SELECT table_name FROM information_schema.tables WHERE table_type = \'TABLE\'').collect { it.table_name.toLowerCase() }
        assert tables as Set == ['book', 'author', 'databasechangeloglock', 'databasechangelog'] as Set
    }

    def "writes SQL to roll back the database to the state it was in when the tag was applied to STDOUT"() {
        when:
            command.handle(getExecutionContext('test-tag'))

        then:
            def output = outputCapture.toString()
            output.contains('DROP TABLE PUBLIC.book;')
            !output.contains('DROP TABLE PUBLIC.author;')
    }

    def "writes SQL to roll back the database to the state it was in when the tag was applied to a file given as arguments"() {
        when:
            command.handle(getExecutionContext('test-tag', outputFile.canonicalPath))

        then:
            def output = outputFile.text
            output.contains('DROP TABLE PUBLIC.book;')
            !output.contains('DROP TABLE PUBLIC.author;')

    }

    def "an error occurs if tagName parameter is not specified"() {
        when:
            command.handle(getExecutionContext())

        then:
            def e = thrown(DatabaseMigrationException)
            e.message == "The ${command.name} command requires a tag"
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
}
'''
}
