FROM ubuntu:jammy-20230624@sha256:b060fffe8e1561c9c3e6dea6db487b900100fc26830b9ea2ec966c151ab4c020

RUN apt update
RUN apt install -y git openjdk-17-jdk unzip wget curl

WORKDIR /android

COPY . .

RUN apt update
RUN apt install -y git openjdk-17-jdk unzip wget

ENV ANDROID_COMMAND_LINE_TOOLS_FILENAME commandlinetools-linux-10406996_latest.zip
ENV ANDROID_API_LEVELS                  android-36,android-37
ENV ANDROID_BUILD_TOOLS_VERSION         35.0.0

ENV ANDROID_HOME /usr/local/android-sdk-linux
ENV ANDROID_SDK_ROOT=/usr/local/android-sdk-linux
ENV PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

# Download and install commandline-tools into cmdline-tools/latest
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    DOWNLOAD_URL=$(curl -s https://developer.android.com/studio \
      | grep -o 'https://dl.google.com/android/repository/commandlinetools-linux-[0-9]*_latest.zip' \
      | head -1) && \
    wget -q "$DOWNLOAD_URL" -O /tmp/commandlinetools.zip && \
    unzip -q /tmp/commandlinetools.zip -d /tmp/cmdline-tools && \
    mv /tmp/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest && \
    rm -rf /tmp/commandlinetools.zip /tmp/cmdline-tools    

RUN yes | sdkmanager --update --sdk_root="${ANDROID_HOME}"
RUN yes | sdkmanager --sdk_root="${ANDROID_HOME}" "platforms;android-36" "platforms;android-37" "build-tools;${ANDROID_BUILD_TOOLS_VERSION}" "extras;google;m2repository" "extras;android;m2repository" "extras;google;google_play_services" "ndk;28.0.13004108" "cmake;3.22.1"

RUN yes | sdkmanager --licenses --sdk_root="${ANDROID_HOME}" && \
    sdkmanager --include_preview \
      "platforms;android-37" \
      "platform-tools"

RUN rm -rf ${ANDROID_HOME}/tools

RUN git config --global --add safe.directory /project
