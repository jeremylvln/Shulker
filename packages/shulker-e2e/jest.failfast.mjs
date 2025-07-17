class FailFastReporter {
  #shouldFail = false;

  /**
   * @param {import("@jest/test-result").Test} test
   * @param {import("@jest/types").Circus.TestCaseStartInfo} testCaseStartInfo
   */
  onTestCaseStart(test, testCaseStartInfo) {
    if (this.#shouldFail) {
      throw new Error('Stopping running tests as one failed');
    }
  }

  /**
   * @param {import("@jest/test-result").Test} test
   * @param {import("@jest/test-result").TestCaseResult} testCastResult
   */
  onTestCaseResult(test, testCastResult) {
    if (testCastResult.status === 'failed') {
      this.#shouldFail = true;
    }
  }
}

export default FailFastReporter;
