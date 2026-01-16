import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.button
import react.useState

val App = FC<Props> {
    var count by useState(0)
    
    div {
        style = jso {
            maxWidth = "800px"
            margin = "0 auto"
            padding = "40px"
            fontFamily = "'Segoe UI', Tahoma, Geneva, Verdana, sans-serif"
            textAlign = "center"
        }
        
        h1 {
            style = jso {
                color = "#333"
                marginBottom = "20px"
            }
            +"이음 프로젝트"
        }
        
        p {
            style = jso {
                fontSize = "18px"
                color = "#666"
                marginBottom = "30px"
            }
            +"Kotlin/JS + Spring Boot + PostgreSQL"
        }
        
        div {
            style = jso {
                padding = "20px"
                backgroundColor = "#f5f5f5"
                borderRadius = "8px"
            }
            
            p {
                +"Count: $count"
            }
            
            button {
                style = jso {
                    padding = "10px 20px"
                    fontSize = "16px"
                    backgroundColor = "#007bff"
                    color = "white"
                    border = "none"
                    borderRadius = "4px"
                    cursor = "pointer"
                    marginTop = "10px"
                }
                
                onClick = {
                    count++
                }
                +"증가"
            }
        }
    }
}
