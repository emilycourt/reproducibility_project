# Start from LTS OpenJDK image
FROM eclipse-temurin:11 as jre-build

# Create a custom Java runtime
RUN $JAVA_HOME/bin/jlink \
         --add-modules java.base,java.desktop,java.xml \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /javaruntime

# Start off with a long-term maintained base distribution
FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive
ENV LANG="C.UTF-8"
ENV LC_ALL="C.UTF-8"

RUN apt update && apt install -y --no-install-recommends \
	biber \
	build-essential \
	ca-certificates \
	curl \
	file \
	gawk \
	git \
	latexmk \
	libpng-dev \
	sudo \
	time \
	texlive-base \
	texlive-bibtex-extra \
	texlive-fonts-recommended \
	texlive-plain-generic \
	texlive-latex-extra \
	texlive-publishers \
	vim

# Add user
RUN useradd -m -G sudo -s /bin/bash repro && echo "repro:repro" | chpasswd
USER repro
WORKDIR /home/repro

# Set up environment variables for Java and YFilter
ENV JAVA_HOME=/opt/java/openjdk
ENV YFILTER_HOME=/home/repro/yfilter-1.0
ENV PATH "${JAVA_HOME}/bin:${YFILTER_HOME}/bin:${PATH}"
ENV CLASSPATH "${YFILTER_HOME}/include/dtdparser113.jar:${YFILTER_HOME}/include/java_cup.jar:${YFILTER_HOME}/build/yfilter.jar"
COPY --from=jre-build /javaruntime $JAVA_HOME

WORKDIR /home/repro

# Get YFilter sources
COPY --chown=repro:repro yfilter-1.0/ yfilter-1.0/

# Get datasets
COPY --chown=repro:repro queries/ queries/

# Copy scripts
COPY --chown=repro:repro --chmod=755 scripts/smoke.sh .

## Clone the paper from the github repository
RUN git clone https://github.com/emilycourt/reproducibility_report.git report