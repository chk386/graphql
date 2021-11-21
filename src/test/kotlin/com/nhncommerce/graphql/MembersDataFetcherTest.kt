package com.nhncommerce.graphql

import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration
import com.netflix.graphql.dgs.autoconfig.DgsExtendedScalarsAutoConfiguration
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest
import com.netflix.graphql.dgs.reactive.DgsReactiveQueryExecutor
import com.netflix.graphql.dgs.webflux.autoconfiguration.DgsWebFluxAutoConfiguration
import com.nhncommerce.graphql.client.FindMemberByIdGraphQLQuery
import com.nhncommerce.graphql.client.FindMemberByIdProjectionRoot
import com.nhncommerce.graphql.types.Member
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode
import reactor.test.StepVerifier
/**
 *
 * @see <a href="https://github.com/Netflix/dgs-examples-kotlin/blob/main/src/test/kotlin/com/example/demo/datafetchers/ShowsDataFetcherTest.kt">TestCase Sample</a>
 *
 */
@SpringBootTest(classes = [DgsAutoConfiguration::class, DgsWebFluxAutoConfiguration::class, DgsExtendedScalarsAutoConfiguration::class, MembersDataFetcher::class, MembersDataFetcher.MyContextBuilder::class])
@TestConstructor(autowireMode = AutowireMode.ALL)
internal class MembersDataFetcherTest(val dgsQueryExecutor: DgsReactiveQueryExecutor) {
    val logger = LoggerFactory.getLogger(this::class.java)!!

    @Test
    fun findMemberById() {
        val id = 10;

        val request = GraphQLQueryRequest(
            FindMemberByIdGraphQLQuery.newRequest().id(id).build(),
            FindMemberByIdProjectionRoot()
                .id()
                .name()
                .company().parent()
                .createdAt()
                .teams()
                .no()
                .name()
                .createdAt().parent()
        )

        logger.debug(request.serialize())

        dgsQueryExecutor.executeAndExtractJsonPathAsObject(
            request.serialize(),
            "data.findMemberById",
            Member::class.java
        )
            .log()
            .map(Member::id)
            .`as`(StepVerifier::create)
            .expectNext(id.toString())
            .verifyComplete()
    }
}