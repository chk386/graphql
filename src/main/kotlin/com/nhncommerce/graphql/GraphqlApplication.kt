package com.nhncommerce.graphql

import com.netflix.graphql.dgs.*
import com.netflix.graphql.dgs.context.DgsContext
import com.netflix.graphql.dgs.reactive.DgsReactiveCustomContextBuilderWithRequest
import com.nhncommerce.graphql.DgsConstants.MEMBER
import com.nhncommerce.graphql.DgsConstants.MEMBER.Teams
import com.nhncommerce.graphql.DgsConstants.QUERY.FindMembers
import com.nhncommerce.graphql.DgsConstants.QUERY_TYPE
import com.nhncommerce.graphql.types.*
import org.reactivestreams.Publisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.time.Duration
import java.time.OffsetDateTime

@SpringBootApplication
@Configuration
class GraphqlApplication {

    @Bean
    fun run(): ApplicationRunner {
        return ApplicationRunner {
            DataBufferUtils.readInputStream({ System.`in` }, DefaultDataBufferFactory(), 1024)
                .map {
                    val asByteBuffer = it.asByteBuffer()
                    val keyInput = String(asByteBuffer.array(), asByteBuffer.arrayOffset(), asByteBuffer.capacity())

                    PushMessage(keyInput, "본문")
                }.subscribe(multicast()::tryEmitNext)
        }
    }

    @Bean
    fun multicast(): Sinks.Many<PushMessage> = Sinks.many().multicast().directBestEffort()
}

fun main(args: Array<String>) {
    runApplication<GraphqlApplication>(*args)
}

@DgsComponent
class MembersDataFetcher(var multicast: Sinks.Many<PushMessage>) {
    val log: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * @RequestHeader headerValue: String?
     * @RequestParam(defaultValue = "key") param: String?
     * @CookieValue(defaultValue = "defaultvalue") cookieValue: String?
     */
    @DgsQuery
    fun findMemberById(@InputArgument id: Int? = 0): Mono<Member> {
        val member = Member(
            id.toString(),
            "this is graphQL",
            Company.COMMERCE,
            listOf(Team(id.toString(), "backoffice", OffsetDateTime.now())),
            OffsetDateTime.now()
        )

        return Mono.just(member)
    }

    @DgsData(parentType = QUERY_TYPE, field = FindMembers)
    fun fetchAll(dfe: DgsDataFetchingEnvironment): Mono<List<Member>> {
        val createdAt = OffsetDateTime.now()
        val context = dfe.getDgsContext().customContext as MyContext

        DgsContext.getCustomContext<MyContext>(dfe)
            .apply { data = "fetchAll에서 보냅니다.~~~" }

        return Mono.just(
            listOf(
                Member("1", "graphQL", Company.COMMERCE, emptyList(), createdAt),
                Member("2", "restful", Company.ACCOMMATE, emptyList(), createdAt),
                Member("3", "websocket", Company.WETOO, emptyList(), createdAt)
            )
        )
    }

    //
    // Child Datafetchers
    @DgsData(parentType = MEMBER.TYPE_NAME, field = Teams)
    fun findTeamsByMemberId(dfe: DgsDataFetchingEnvironment): Mono<List<Team>> {
        val member = dfe.getSource<Member>()

        DgsContext.getCustomContext<MyContext>(dfe)
            .also { println(it.data) }

        return Mono.just(
            listOf(
                Team(member.id, "backoffice", OffsetDateTime.now()),
                Team(member.id, "BE", OffsetDateTime.now()),
                Team(member.id, "FE", OffsetDateTime.now())
            )
        )
    }

    @DgsMutation
    fun createMember(
        @InputArgument("memberInput") memberInput: MemberInput,
        dfe: DgsDataFetchingEnvironment
    ): Mono<Member> {
        val name = dfe.getArgument<String>("name");

        log.debug("inputArgument : $memberInput")
        log.debug("name : $name")

        return Mono.just(Member("100000", "created", Company.COMMERCE, emptyList(), OffsetDateTime.now()))
    }

    /**
     * <p>data fetcher context</p>
     * <p>The context is initialized per request, before query execution starts.</p>
     *
     * fetchAll()에서 dgsContext에 값을 넣고 -> findTeamsByMemberId에서 꺼내올수 있다.
     * request정보를 context에 넣고 싶을 경우 DgsCustomContextBuilderWithRequest를 구현하라.
     */
    @DgsComponent
    class MyContextBuilder : DgsReactiveCustomContextBuilderWithRequest<MyContext> {
        override fun build(
            extensions: Map<String, Any>?,
            headers: HttpHeaders?,
            serverRequest: ServerRequest?
        ) = Mono.just(MyContext("Hello"))
    }

    data class MyContext(val customState: String, var data: String = "MyContext Data!!")

    @DgsSubscription
    fun push(): Publisher<PushMessage> {
        return multicast.asFlux()
    }

//    @DgsSubscription
//    fun push(): Publisher<PushMessage> {
//        return Flux.interval(Duration.ofSeconds(1))
//            .map { PushMessage("${it.toString()} + 번째 타이틀", "개발실 여러분 하이!") }
//    }
}