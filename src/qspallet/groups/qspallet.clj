(ns qspallet.groups.qspallet
    "Node defintions for qspallet"
    (:require
     [pallet.api :as api]
     [pallet.configure :as configure]
     [pallet.crate.automated-admin-user :as admin-user]))

(def default-node-spec
  (api/node-spec
   :image {:os-family :ubuntu :os-version-matches "12.04"}
   :hardware {:min-ram (* 15 1024)}
   :network {:inbound-ports [22]}))

;; GreenQloud doesn't have Ubuntu 12.04 yet
(def greenqloud-node-spec
  (api/node-spec
   :image {:os-family :ubuntu :os-version-matches "10.04"}
   :hardware {:min-ram (* 15 1024)}
   :network {:inbound-ports [22]}))

(def
  ^{:doc "Defines the type of node qspallet will run on"}
  base-server
  (api/server-spec
   :phases
   {:bootstrap (api/plan-fn
                 (admin-user/automated-admin-user)
                 )}))

(def
  ^{:doc "Define a server spec for qspallet"}
  qspallet-server
  (api/server-spec
   :phases
   {:configure (api/plan-fn
                 ;; Add your crates here
                 )}))

(def
  ^{:doc "Defines a group spec that can be passed to converge or lift."}
  qspallet
  (api/group-spec
   "qspallet"
   :extends [base-server qspallet-server]
   :node-spec default-node-spec))

(def
  ^{:doc "Defines a group spec that can be passed to converge or lift."}
  greenqloud-pallet
  (api/group-spec
   "greenqloud-pallet"
   :extends [base-server qspallet-server]
   :node-spec greenqloud-node-spec))

(comment

  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  ;; AWS
  ;;
  ;; If you have to use AWS at least use the greenest AWS locations
  ;; see https://www.mastodonc.com/dashboard for more info
  ;;
  ;; In this example we're using the B availability zone of sa-east-1,
  ;; asking for an m1.large at a spot price of no higher than USD 0.50

  ;; start 'em up
  (api/converge
   (api/group-spec "mygroup"
                   :count 1
                   :node-spec (api/node-spec
                               :image {:os-family :ubuntu
                                       :os-64-bit true}
                               :hardware {:hardware-id "m1.large"}
                               ;; Sao Paulo is the greenest AWS Location
                               :location {:location-id "sa-east-1b"}
                               ;; Oregon is the greenest cheap location (same price as VA)
                               ;;:location {:location-id "us-west-2b"}
                               :qos {:spot-price (float 0.50)}))
   :compute (configure/compute-service :aws))

  ;; shut 'em down
  (api/converge
   (api/group-spec "mygroup"
                   :count 0)
   :compute (configure/compute-service :aws))

  ;; or the same using the defined qspallet group above
  (api/converge
   (assoc qspallet :count 1)
   :compute (configure/compute-service :aws))

  (api/converge
   (assoc qspallet :count 0)
   :compute (configure/compute-service :aws))
  
  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  ;; Rackspace OpenStack in London
  ;;
  ;; Rackspace in London are Carbon Neutral due to a power purchase
  ;; agreement in the UK.

  ;; start 'em up
  (api/converge
   (assoc qspallet :count 1)
   :compute (configure/compute-service :rs-openstack-uk))

  ;; shut 'em down
  (api/converge
   (assoc qspallet :count 0)
   :compute (configure/compute-service :rs-openstack-uk))

  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  ;; GreenQloud
  ;;
  ;; GreenQloud are in Iceland and run on all renewable electricity.

  ;; start 'em up
  (api/converge
   (assoc greenqloud-pallet :count 1)
   :compute (configure/compute-service :greenqloud))

  ;; shut 'em down
  (api/converge
   (assoc greenqloud-pallet :count 0)
   :compute (configure/compute-service :greenqloud))
  
  )
