package com.nhncommerce.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import com.nhncommerce.graphql.client.FindMemberByIdProjectionRoot
import com.nhncommerce.graphql.types.Company
import com.nhncommerce.graphql.types.Member
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.time.OffsetDateTime

@SpringBootApplication
class GraphqlApplication

fun main(args: Array<String>) {
	runApplication<GraphqlApplication>(*args)
}

@Bean
fun run() = ApplicationRunner {

}

@DgsComponent
class MembersDataFetcher {
	@DgsQuery
	fun findMemberById(@InputArgument id: Int): Member {
		return Member(id.toString(), "this is graphQL", Company.COMMERCE, listOf(), OffsetDateTime.now())
	}
}
