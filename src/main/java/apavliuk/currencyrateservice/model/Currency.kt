package apavliuk.currencyrateservice.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table
class Currency(
    @Id
    val id: Long? = null,
    val name: String
)