# quick-question

> A babashka script (and installer) that lets you use the beautiful [enquirer.js](https://github.com/enquirer) but with our superior edn data.

## installation

I would copy this into some init script, maybe into your babashka preambles?

`install.clj`
``` clojure
#! /opt/homebrew/bin/bb

(ns install
  (:require [babashka.deps :as deps]
            [babashka.fs :as fs]
            [babashka.tasks :refer [shell]]))

(defmacro install!
  "This is only a macro because qq/set-path! wasn't being found."
  []
 `(do (when-not (fs/exists? "quick-question")
         (println "Need to do some first time setup, please be patient...")
         (shell "git clone https://github.com/escherize/quick-question.git")
         (shell {:dir "./quick-question"} "npm install enquirer")
         (println "We're ready to go."))
       (deps/add-deps '{:deps {qq/qq {:local/root "./quick-question"}}})
       (require '[qq :as qq])
       (qq/set-path! (str (str (fs/parent *file*)) "/quick-question/qq.js"))))

(install!)

;; optional:
(qq/ask! [{:type :confirm :name :question :message "Do you see a prompt?"}])

```

Run it with `bb install.clj`

## Usage

The main function to use thing, is [qq/ask!](https://github.com/escherize/quick-question/blob/master/qq.clj#L19). You can pass it 1 single map, or a collection of maps. These maps describe questions that enquirer.js will quickly ask you.

## Self Documentation

See: [qq/tutorial](https://github.com/escherize/quick-question/blob/master/qq.clj#L125)

## FAQ

> Why not just use edn.deps like a git sha or what not?

This project needs things from npm (sorry), so we actually npm install enquirer on the first run. 
