FROM ubuntu:20.04

USER root

ENV LD_LIBRARY_PATH /opt/intel/compilers_and_libraries_2018.5.274/linux/mkl/lib/intel64_lin:/opt/intel/compilers_and_libraries_2018.5.274/linux/compiler/lib/intel64_lin
ENV JAVA_OPTS="-Dclojure.compiler.direct-linking=true -XX:MaxDirectMemorySize=16g -XX:+UseLargePages --add-opens=java.base/jdk.internal.ref=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED"
ENV JAVA_TOOL_OPTIONS="-XX:MaxDirectMemorySize=2048M"
EXPOSE 22
EXPOSE 8888

RUN apt update
RUN apt install -y wget
RUN apt install -y gnupg2
RUN apt install -y openjdk-17-jdk
RUN apt install -y clojure
RUN apt install -y openssh-server
RUN mkdir /var/run/sshd
RUN echo 'root:root123' | chpasswd
RUN sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config
#RUN apt install -y apt-transport-https
#RUN wget -O - https://apt.repos.intel.com/intel-gpg-keys/GPG-PUB-KEY-INTEL-SW-PRODUCTS-2019.PUB | apt-key add -
#RUN sh -c 'echo deb https://apt.repos.intel.com/mkl all main > /etc/apt/sources.list.d/intel-mkl.list'
#RUN apt update && apt install -y intel-mkl-64bit-2018.4-057
#RUN apt install --assume-yes intel-mkl=2020.0.166-1

RUN apt install -y curl
RUN apt install -y rlwrap
RUN apt install -y git
RUN /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
RUN curl -L -O https://github.com/clojure/brew-install/releases/latest/download/linux-install.sh
RUN chmod +x linux-install.sh
RUN ./linux-install.sh

RUN echo '{:deps {  \
                  uncomplicate/neanderthal {:mvn/version "0.45.0"}  \
                  nrepl/nrepl {:mvn/version "1.0.0"}  \
                  org.clojure/tools.deps.alpha {:mvn/version "0.15.1254"}  \
                  cider/cider-nrepl {:mvn/version "0.30.0"}  \
                  org.clojure/data.csv {:mvn/version "1.0.1"}  \
                  criterium/criterium {:mvn/version "0.4.6"}  \
                 }  \
           :aliases {:nREPL {:extra-deps {nrepl/nrepl {:mvn/version "1.0.0"}}}} \
          }' > deps.edn &&\
    echo '{:bind "0.0.0.0" :port 8888}' > .nrepl.edn &&\
    clj -Sforce < /dev/null >&0

CMD /usr/sbin/sshd & clj -A:nREPL -m nrepl.cmdline --middleware [cider.nrepl/cider-middleware]