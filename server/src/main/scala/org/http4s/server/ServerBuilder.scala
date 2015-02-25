package org.http4s.server

import java.net.{InetAddress, InetSocketAddress}
import java.util.concurrent.ExecutorService

import com.codahale.metrics.MetricRegistry
import org.http4s.server.SSLSupport.StoreInfo

import scala.concurrent.duration._
import scalaz.concurrent.{Strategy, Task}

trait ServerBuilder {
  import ServerBuilder._

  type Self <: ServerBuilder

  def bindSocketAddress(socketAddress: InetSocketAddress): Self

  final def bindHttp(port: Int = DefaultHttpPort, host: String = DefaultHost) =
    bindSocketAddress(InetSocketAddress.createUnresolved(host, port))

  final def bindLocal(port: Int) = bindHttp(port, DefaultHost)

  final def bindAny(host: String = DefaultHost) = bindHttp(0, host)

  def withServiceExecutor(executorService: ExecutorService): Self

  def mountService(service: HttpService, prefix: String = ""): Self

  def start: Task[Server]

  final def run: Server = start.run
}

object ServerBuilder {
  // Defaults for core server builder functionality
  val LoopbackAddress = InetAddress.getLoopbackAddress.getHostAddress
  val DefaultHost = LoopbackAddress
  val DefaultHttpPort = 8080
  val DefaultSocketAddress = InetSocketAddress.createUnresolved(DefaultHost, DefaultHttpPort)
  val DefaultServiceExecutor = Strategy.DefaultExecutorService
}

trait IdleTimeoutSupport { this: ServerBuilder =>
  def withIdleTimeout(idleTimeout: Duration): Self
}
object IdleTimeoutSupport {
  val DefaultIdleTimeout = 30.seconds
}

trait AsyncTimeoutSupport { this: ServerBuilder =>
  def withAsyncTimeout(asyncTimeout: Duration): Self
}
object AsyncTimeoutSupport {
  val DefaultAsyncTimeout = 30.seconds
}

trait SSLSupport { this: ServerBuilder =>
  def withSSL(keyStore: StoreInfo,
    keyManagerPassword: String,
              protocol: String = "TLS",
            trustStore: Option[StoreInfo] = None,
            clientAuth: Boolean = false): Self
}
object SSLSupport {
  case class StoreInfo(path: String, password: String)
  case class SSLBits(keyStore: StoreInfo,
           keyManagerPassword: String,
                     protocol: String,
                   trustStore: Option[StoreInfo],
                   clientAuth: Boolean)
}

trait MetricsSupport { this: ServerBuilder =>
  /**
   * Triggers collection of backend-specific Metrics into the specified [[MetricRegistry]].
   */
  def withMetricRegistry(metricRegistry: MetricRegistry): Self

  /** Sets the prefix for metrics gathered by the server.*/
  def withMetricPrefix(metricPrefix: String): Self
}
object MetricsSupport {
  val DefaultPrefix = "org.http4s.server"
}
