(ns fastcgi-ring.core
  (:use ring.util.response))

(deftype ring-request-adpater* [request root]
  net.jr.fastcgi.impl.RequestAdapter
  (getServletPath [_] (get request :uri))
  (getRealPath [_ path] (str root path))
  (getContextPath [_] "")
  (getRemoteUser [_] nil)
  (getAuthType [_] nil)
  (getHeaderNames [_] (.elements (java.util.Vector. (or (keys (get request :headers)) []))))
  (getHeader [_ k] (get k (get request :headers)))
  (getInputStream [_] (get request :body))
  (getRequestURI [_] (get request :uri))
  (getMethod [_] (name (get request :request-method)))
  (getServerName [_] (get request :server-name))
  (getServerPort [_] (get request :server-port))
  (getRemoteAddr [_] (get request :remote-addr))
  (getProtocol [_] (name (get request :scheme)))
  (getQueryString [_] (get request :query-string ""))
  (getContentLength [_] (get request :content-length -1)))

(defn ring-request-adpater [request root]
  (ring-request-adpater*. (into {} (filter val request)) root))

(defn ring-response-adapter []
  (let [out (java.io.ByteArrayOutputStream.)
        ring-resp (atom (response out))]
    [(reify net.jr.fastcgi.impl.ResponseAdapter
       (setStatus [_ s] (swap! ring-resp status s))
       (sendError [this e] (.setStatus this e))
       (addHeader [_ k v] (swap! ring-resp header k v))
       (getOutputStream [_] out)
       (sendRedirect [_ url] (redirect url)))
     ring-resp]))

(defn fastcgi [host port root]
  (let [handler (net.jr.fastcgi.impl.FastCGIHandler.)
        con (net.jr.fastcgi.impl.SingleConnectionFactory.
              (java.net.InetAddress/getByName host)
              port)]
    (.setConnectionFactory handler con)
    (fn [req]
      (let [req (ring-request-adpater req root)
            [resp ring-resp] (ring-response-adapter)]
        (.service handler req resp)
        (swap! ring-resp update-in [:body] #(java.io.ByteArrayInputStream. (.toByteArray %)))
        @ring-resp))))
