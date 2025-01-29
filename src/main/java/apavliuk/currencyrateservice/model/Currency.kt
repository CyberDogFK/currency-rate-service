package apavliuk.currencyrateservice.model

import io.github.joselion.springr2dbcrelationships.annotations.OneToMany
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table
class Currency(
    @Id
    val id: Long? = null,
    val name: String,
    @Column(value = "type_id")
    @OneToMany val type: CurrencyType
)