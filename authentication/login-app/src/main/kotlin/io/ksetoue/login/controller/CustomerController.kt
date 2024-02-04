package io.ksetoue.login.controller

import io.ksetoue.login.domain.Customer
import io.ksetoue.login.domain.CustomerRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import java.security.Principal

@Controller
class CustomerController {

    @Autowired
    private val customerDAO: CustomerRepository? = null

    @GetMapping(path = ["/"])
    fun index(): String? {
        return "external"
    }

    @GetMapping("/logout")
    @Throws(Exception::class)
    fun logout(request: HttpServletRequest): String? {
        request.logout()
        return "redirect:/"
    }

    @GetMapping(path = ["/customers"])
    fun customers(principal: Principal, model: Model): String? {
        addCustomers()
        val customers: Iterable<Customer> = customerDAO?.findAll() ?: throw Throwable("customer list is empty")
        model.addAttribute("customers", customers)
        model.addAttribute("username", principal.name)
        return "customers"
    }

    // add customers for demonstration
    fun addCustomers() {
        val customer1 = Customer()
        customer1.address = "1111 foo blvd"
        customer1.name = "Foo Industries"
        customer1.serviceRendered = "Important services"
        customerDAO?.save(customer1)
        val customer2 = Customer()
        customer2.address = "2222 bar street"
        customer2.name = "Bar LLP"
        customer2.serviceRendered = "Important services"
        customerDAO?.save(customer2)
        val customer3 = Customer()
        customer3.address = "33 main street"
        customer3.name = "Big LLC"
        customer3.serviceRendered = "Important services"
        customerDAO!!.save(customer3)
    }
}